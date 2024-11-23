package com.westminster.ticketing_system.core.pool;

import com.westminster.ticketing_system.entity.Ticket;
import com.westminster.ticketing_system.entity.User;
import com.westminster.ticketing_system.enums.Transaction;
import com.westminster.ticketing_system.enums.UserRole;
import com.westminster.ticketing_system.repository.TicketRepository;
import com.westminster.ticketing_system.repository.UserRepository;
import com.westminster.ticketing_system.services.admin.AdminService;
import com.westminster.ticketing_system.dtos.SystemConfigurationDTO;
import com.westminster.ticketing_system.dtos.TransactionLogDTO;
import com.westminster.ticketing_system.services.transaction.TransactionLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.westminster.ticketing_system.services.systemLog.SystemLogService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

/**
 * Implementation of the TicketPool interface that manages concurrent ticket
 * operations.
 * Handles ticket addition by vendors and purchases by customers using
 * thread-safe mechanisms.
 */
@Component
@Slf4j
public class TicketPoolImplementation implements TicketPool {
    private static final String SOURCE = "TicketPoolImplementation";
    private static final String ORIGINATOR = "SYSTEM";

    private final AdminService adminService;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final int maxPoolSize;
    private final ConcurrentLinkedQueue<Ticket> ticketQueue;
    private final Object lock = new Object();
    private volatile boolean isRunning;
    private final TransactionLogService transactionLogService;
    private final SystemLogService logService;

    // Semaphore to track how many tickets are available for purchase
    // Starts at 0 and increases when tickets are added, decreases when purchased
    private final Semaphore availableTickets;
    // Semaphore to track how many tickets can be added to the pool
    // Starts at maxPoolSize and decreases when tickets are added
    private final Semaphore capacityControl;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TicketPoolImplementation(
            AdminService adminService,
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TransactionLogService transactionLogService,
            SystemLogService logService) {

        this.adminService = adminService;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.transactionLogService = transactionLogService;
        this.logService = logService;
        this.isRunning = true;

        SystemConfigurationDTO config = adminService.getSystemConfiguration();
        this.maxPoolSize = config.getMaxTicketCapacity();
        this.ticketQueue = new ConcurrentLinkedQueue<>();
        this.availableTickets = new Semaphore(0, true);
        this.capacityControl = new Semaphore(maxPoolSize, true);

        logService.info(SOURCE, "Initialized ticket pool with max capacity: " + maxPoolSize, ORIGINATOR,
                "TicketPoolImplementation");
    }

    /**
     * Adds specified number of tickets to the pool from a vendor.
     * Handles rate limiting and capacity control.
     *
     * @param ticketCount Number of tickets to add
     * @param vendorId    ID of the vendor adding tickets
     * @return boolean indicating success of operation
     * @throws InterruptedException if thread is interrupted during operation
     */
    @Override
    public boolean addTickets(int ticketCount, int vendorId) throws InterruptedException {
        notifyPoolStatusChange(); // Notify clients of the current pool status
        if (!isRunning) {
            logService.warn(SOURCE, "Ticket pool is not running - rejecting tickets from vendor " + vendorId,
                    ORIGINATOR, "addTickets");
            return false;
        }

        try {
            int currentCount = getCurrentTicketCount();
            int availableCapacity = maxPoolSize - currentCount;

            if (availableCapacity <= 0) {
                logService.warn(SOURCE, "Pool is at capacity (" + maxPoolSize + ") - rejecting tickets from vendor "
                        + vendorId, ORIGINATOR, "addTickets");
                return false;
            }

            if (availableCapacity < ticketCount) {
                logService.warn(SOURCE, "Pool does not have enough space for " + ticketCount
                        + " tickets - rejecting tickets from vendor " + vendorId, ORIGINATOR, "addTickets");
                return false;
            }

            SystemConfigurationDTO config = adminService.getSystemConfiguration();
            int releaseRate = config.getTicketReleaseRate();
            List<Ticket> tickets = generateTicketBatch(ticketCount, vendorId);

            for (Ticket ticket : tickets) {
                Thread.sleep(releaseRate); // Rate limiting: Controls how fast tickets can be added

                if (!capacityControl.tryAcquire(5, TimeUnit.SECONDS)) { // Timeout: Maximum time to wait for space to
                                                                        // become available
                    logService.error(SOURCE, "Capacity control timeout for vendor " + vendorId, ORIGINATOR,
                            "addTickets");
                    return false;
                }

                try {
                    synchronized (lock) {
                        Ticket savedTicket = ticketRepository.save(ticket);
                        ticketQueue.offer(savedTicket);
                        availableTickets.release(); // Release one permit to availableTickets
                        // This signals that one more ticket is available for purchase

                        TransactionLogDTO transactionLog = TransactionLogDTO.createSuccessfulTransaction(
                                Transaction.SALE, vendorId, UserRole.VENDOR,
                                savedTicket.getId(), savedTicket.getPrice());
                        transactionLogService.addTransaction(transactionLog);

                        logService.debug(SOURCE, "Added ticket " + savedTicket.getId()
                                + " to pool - current size: " + ticketQueue.size(), ORIGINATOR,
                                "addTickets");
                    }
                } catch (Exception e) {
                    logService.error(SOURCE, "Failed to save ticket for vendor " + vendorId + ": "
                            + e.getMessage(), ORIGINATOR, "addTickets");
                    capacityControl.release(); // If saving fails, release the capacity we acquired
                    throw e;
                }
            }
            logService.info(SOURCE, "Successfully added " + ticketCount + " tickets from vendor "
                    + vendorId, ORIGINATOR, "addTickets");
            notifyPoolStatusChange();
            return true;

        } catch (InterruptedException e) {
            logService.error(SOURCE, "Ticket addition interrupted for vendor " + vendorId, ORIGINATOR,
                    "addTickets");
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            logService.error(SOURCE, "Unexpected error adding tickets for vendor " + vendorId + ": "
                    + e.getMessage(), ORIGINATOR, "addTickets");
            throw e;
        }
    }

    /**
     * Processes ticket purchase requests from customers.
     * Handles rate limiting and availability control.
     *
     * @param count      Number of tickets to purchase
     * @param customerId ID of the customer making the purchase
     * @return boolean indicating success of operation
     * @throws InterruptedException if thread is interrupted during operation
     */
    @Override
    public boolean purchaseTickets(int count, int customerId) throws InterruptedException {
        notifyPoolStatusChange(); // Notify clients of the current pool status
        if (!isRunning) {
            logService.warn(SOURCE, "Ticket pool is not running - rejecting purchase from customer "
                    + customerId, ORIGINATOR, "purchaseTickets");
            return false;
        }

        try {
            SystemConfigurationDTO config = adminService.getSystemConfiguration();
            long retrievalRate = config.getCustomerRetrievalRate();
            List<Ticket> purchasedTickets = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                Thread.sleep(retrievalRate);

                if (!availableTickets.tryAcquire(5, TimeUnit.SECONDS)) {
                    logService.warn(SOURCE, "No tickets available for customer " + customerId
                            + " after timeout", ORIGINATOR, "purchaseTickets");
                    return false;
                }

                try {
                    synchronized (lock) {
                        Ticket ticket = ticketQueue.poll();
                        if (ticket != null) {
                            ticket.setPurchaser(userRepository.findById(customerId).get());
                            ticket.setPurchasedDateTime(LocalDateTime.now());
                            purchasedTickets.add(ticket);
                            capacityControl.release(); // Release one permit back to capacityControl
                            // This signals that we have space for one more ticket in the pool

                            TransactionLogDTO transactionLog = TransactionLogDTO.createSuccessfulTransaction(
                                    Transaction.PURCHASE, customerId, UserRole.CUSTOMER,
                                    ticket.getId(), ticket.getPrice());
                            transactionLogService.addTransaction(transactionLog);

                            logService.debug(SOURCE, "Customer " + customerId
                                    + " purchased ticket " + ticket.getId()
                                    + " - pool size: " + ticketQueue.size(), ORIGINATOR,
                                    "purchaseTickets");
                        }
                    }
                } catch (Exception e) {
                    logService.error(SOURCE, "Error processing purchase for customer " + customerId + ": "
                            + e.getMessage(), ORIGINATOR, "purchaseTickets");
                    availableTickets.release(); // If purchase fails, release the ticket we acquired
                    throw e;
                }
            }

            ticketRepository.saveAll(purchasedTickets);
            logService.info(SOURCE, "Customer " + customerId + " successfully purchased " + count
                    + " tickets", ORIGINATOR, "purchaseTickets");
            notifyPoolStatusChange();
            return true;

        } catch (InterruptedException e) {
            logService.error(SOURCE, "Purchase interrupted for customer " + customerId, ORIGINATOR,
                    "purchaseTickets");
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            logService.error(SOURCE, "Unexpected error during purchase for customer " + customerId + ": "
                    + e.getMessage(), ORIGINATOR, "purchaseTickets");
            throw e;
        }
    }

    /**
     * Gracefully shuts down the ticket pool.
     * Prevents new tickets from being added or purchased while allowing ongoing
     * transactions to complete.
     */
    @Override
    public void shutdown() {
        logService.info(SOURCE, "Initiating ticket pool shutdown", ORIGINATOR,
                "shutdown");
        isRunning = false;
        logService.info(SOURCE, "Ticket pool has been shut down. No new transactions will be accepted",
                ORIGINATOR, "shutdown");
    }

    /**
     * Returns the current number of available tickets in the pool.
     * This is a lightweight operation that provides a snapshot of the pool size.
     *
     * @return The current number of tickets available for purchase
     */
    @Override
    public int getCurrentTicketCount() {
        int count = ticketQueue.size();
        logService.debug(SOURCE, "Current ticket count in pool: " + count, ORIGINATOR,
                "getCurrentTicketCount");
        return count;
    }

    /**
     * Checks if the ticket pool has reached its maximum capacity.
     * Maximum capacity is determined by the system configuration and can be
     * adjusted dynamically.
     *
     * @return true if the pool is at capacity, false otherwise
     */
    @Override
    public boolean isPoolFull() {
        SystemConfigurationDTO config = adminService.getSystemConfiguration();
        int maxCapacity = config.getMaxTicketCapacity();
        boolean isFull = ticketQueue.size() >= maxCapacity;
        logService.debug(SOURCE, "Pool capacity check - Current size: " + ticketQueue.size()
                + ", Max capacity: " + maxCapacity + ", Is full: " + isFull, ORIGINATOR,
                "isPoolFull");
        return isFull;
    }

    /**
     * Checks if the ticket pool has no available tickets.
     *
     * @return true if there are no tickets available, false otherwise
     */
    @Override
    public boolean isPoolEmpty() {
        boolean isEmpty = ticketQueue.isEmpty();
        logService.debug(SOURCE, "Pool empty check - Is empty: " + isEmpty, ORIGINATOR,
                "isPoolEmpty");
        return isEmpty;
    }

    /**
     * Checks if the ticket pool is currently operational.
     *
     * @return true if the pool is accepting transactions, false if shutdown
     */
    @Override
    public boolean isRunning() {
        logService.debug(SOURCE, "Pool status check - Is running: " + isRunning, ORIGINATOR,
                "isRunning");
        return isRunning;
    }

    /**
     * Generates a batch of tickets with specified properties.
     * 
     * @param ticketCount Number of tickets to generate
     * @param vendorId    ID of the vendor creating the tickets
     * @return List of generated ticket entities
     * @throws IllegalArgumentException if vendorId is invalid
     * @throws IllegalStateException    if ticket generation fails
     */
    private List<Ticket> generateTicketBatch(int ticketCount, int vendorId) {
        logService.info(SOURCE, "Generating batch of " + ticketCount + " tickets for vendor " + vendorId,
                ORIGINATOR, "generateTicketBatch");
        List<Ticket> tickets = new ArrayList<>();

        try {
            logService.debug(SOURCE, "Looking up vendor with ID: " + vendorId, ORIGINATOR, "generateTicketBatch");

            Optional<User> optionalVendor = userRepository.findById(vendorId);
            if (!optionalVendor.isPresent()) {
                logService.error(SOURCE, "Vendor not found with ID: " + vendorId, ORIGINATOR, "generateTicketBatch");
                throw new IllegalArgumentException("Invalid vendor ID: " + vendorId);
            }

            User vendor = optionalVendor.get();
            logService.debug(SOURCE, "Successfully found vendor: " + vendor.getUsername(), ORIGINATOR,
                    "generateTicketBatch");

            for (int i = 0; i < ticketCount; i++) {
                Ticket ticket = new Ticket();
                LocalDateTime now = LocalDateTime.now();

                ticket.setTitle("Test Ticket " + i);
                ticket.setDescription("Test Description for ticket " + i);
                ticket.setUpdatedDateTime(now);
                ticket.setUser(vendor);
                ticket.setPrice(BigDecimal.valueOf(100.00));
                ticket.setCreatedDateTime(now);

                tickets.add(ticket);

                logService.debug(SOURCE, "Generated ticket - ID: " + ticket.getId()
                        + ", Title: " + ticket.getTitle()
                        + ", Price: $" + ticket.getPrice()
                        + ", VendorId: " + vendorId, ORIGINATOR,
                        "generateTicketBatch");
            }

            logService.info(SOURCE, "Successfully generated " + ticketCount
                    + " tickets for vendor " + vendorId, ORIGINATOR,
                    "generateTicketBatch");
            return tickets;

        } catch (Exception e) {
            logService.error(SOURCE, "Failed to generate ticket batch - Vendor: " + vendorId
                    + ", Count: " + ticketCount + ", Error: " + e.getMessage(), ORIGINATOR,
                    "generateTicketBatch");
            throw new IllegalStateException("Failed to generate tickets", e);
        }
    }

    private int getAvailableSpace() {
        return maxPoolSize - getCurrentTicketCount();
    }

    /**
     * Broadcasts pool status changes to connected clients via WebSocket.
     * This method is called after significant pool state changes to keep clients
     * informed.
     * 
     * @throws RuntimeException if the WebSocket message cannot be sent
     */
    private void notifyPoolStatusChange() {
        try {
            Map<String, Object> status = Map.of(
                    "currentTicketCount", getCurrentTicketCount(),
                    "isFull", isPoolFull(),
                    "isEmpty", isPoolEmpty(),
                    "availableSpace", getAvailableSpace(),
                    "isRunning", isRunning());

            logService.debug(SOURCE, "Broadcasting pool status update - Status: " + status, ORIGINATOR,
                    "notifyPoolStatusChange");
            messagingTemplate.convertAndSend("/topic/pool-status", status);

        } catch (Exception e) {
            logService.error(SOURCE, "Failed to broadcast pool status update: " + e.getMessage(), ORIGINATOR,
                    "notifyPoolStatusChange");
            throw new RuntimeException("Failed to notify pool status change", e);
        }
    }
}