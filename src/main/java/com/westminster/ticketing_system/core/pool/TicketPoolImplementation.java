package com.westminster.ticketing_system.core.pool;

import com.westminster.ticketing_system.entity.Ticket;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the TicketPool interface that manages concurrent ticket
 * operations.
 * Handles ticket addition by vendors and purchases by customers using
 * thread-safe mechanisms.
 */
@Component
@Slf4j
public class TicketPoolImplementation implements TicketPool {
    private final AdminService adminService;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final int maxPoolSize;
    private final ConcurrentLinkedQueue<Ticket> ticketQueue;
    private final Object lock = new Object();
    private volatile boolean isRunning;
    private final TransactionLogService transactionLogService;

    // Semaphore to track how many tickets are available for purchase
    // Starts at 0 and increases when tickets are added, decreases when purchased
    private final Semaphore availableTickets;
    // Semaphore to track how many tickets can be added to the pool
    // Starts at maxPoolSize and decreases when tickets are added
    private final Semaphore capacityControl;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TicketPoolImplementation(AdminService adminService, TicketRepository ticketRepository,
            UserRepository userRepository, TransactionLogService transactionLogService) {
        this.adminService = adminService;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.transactionLogService = transactionLogService;
        this.isRunning = true;

        // Determine maxPoolSize before using it
        int poolSize = 500; // Default value
        try {
            SystemConfigurationDTO config = adminService.getSystemConfiguration();
            if (config != null) {
                poolSize = config.getMaxTicketCapacity();
                log.info("Loaded maxPoolSize from config: {}", poolSize);
            }
        } catch (Exception e) {
            log.warn("Could not load system configuration. Using default maxPoolSize: {}", poolSize);
        }

        this.maxPoolSize = poolSize;
        this.ticketQueue = new ConcurrentLinkedQueue<>();
        this.availableTickets = new Semaphore(0, true);
        this.capacityControl = new Semaphore(maxPoolSize, true);
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
            log.warn("Ticket pool is not running - rejecting tickets from vendor {}", vendorId);
            return false;
        }

        try {
            int currentCount = getCurrentTicketCount();
            int availableCapacity = maxPoolSize - currentCount;

            if (availableCapacity <= 0) {
                log.warn("Pool is at capacity ({}) - rejecting tickets from vendor {}",
                        maxPoolSize, vendorId);
                return false;
            }

            int adjustedTicketCount = Math.min(ticketCount, availableCapacity);
            if (adjustedTicketCount < ticketCount) {
                log.info("Adjusting request from {} tickets to {} due to capacity constraints",
                        ticketCount, adjustedTicketCount);
            }

            SystemConfigurationDTO config = adminService.getSystemConfiguration();
            long releaseRate = config.getTicketReleaseRate();
            List<Ticket> tickets = generateTicketBatch(adjustedTicketCount, vendorId);

            for (Ticket ticket : tickets) {
                Thread.sleep(releaseRate); // Rate limiting: Controls how fast tickets can be added

                if (!capacityControl.tryAcquire(5, TimeUnit.SECONDS)) { // Timeout: Maximum time to wait for space to
                                                                        // become available
                    log.error("Capacity control timeout for vendor {}", vendorId);
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

                        log.debug("Added ticket {} to pool - current size: {}",
                                savedTicket.getId(), ticketQueue.size());
                    }
                } catch (Exception e) {
                    log.error("Failed to save ticket for vendor {}: {}",
                            vendorId, e.getMessage(), e);
                    capacityControl.release(); // If saving fails, release the capacity we acquired
                    throw e;
                }
            }
            log.info("Successfully added {} tickets from vendor {}", adjustedTicketCount, vendorId);
            notifyPoolStatusChange();
            return true;

        } catch (InterruptedException e) {
            log.error("Ticket addition interrupted for vendor {}", vendorId);
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error adding tickets for vendor {}: {}",
                    vendorId, e.getMessage(), e);
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
            log.warn("Ticket pool is not running - rejecting purchase from customer {}", customerId);
            return false;
        }

        try {
            SystemConfigurationDTO config = adminService.getSystemConfiguration();
            long retrievalRate = config.getCustomerRetrievalRate();
            List<Ticket> purchasedTickets = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                Thread.sleep(retrievalRate);

                if (!availableTickets.tryAcquire(5, TimeUnit.SECONDS)) {
                    log.warn("No tickets available for customer {} after timeout", customerId);
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

                            log.debug("Customer {} purchased ticket {} - pool size: {}",
                                    customerId, ticket.getId(), ticketQueue.size());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing purchase for customer {}: {}",
                            customerId, e.getMessage(), e);
                    availableTickets.release(); // If purchase fails, release the ticket we acquired
                    throw e;
                }
            }

            ticketRepository.saveAll(purchasedTickets);
            log.info("Customer {} successfully purchased {} tickets", customerId, count);
            notifyPoolStatusChange();
            return true;

        } catch (InterruptedException e) {
            log.error("Purchase interrupted for customer {}", customerId);
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during purchase for customer {}: {}",
                    customerId, e.getMessage(), e);
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
        log.info("Initiating ticket pool shutdown");
        isRunning = false;
        log.info("Ticket pool has been shut down. No new transactions will be accepted");
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
        log.debug("Current ticket count in pool: {}", count);
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
        log.debug("Pool capacity check - Current size: {}, Max capacity: {}, Is full: {}",
                ticketQueue.size(), maxCapacity, isFull);
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
        log.debug("Pool empty check - Is empty: {}", isEmpty);
        return isEmpty;
    }

    /**
     * Checks if the ticket pool is currently operational.
     *
     * @return true if the pool is accepting transactions, false if shutdown
     */
    @Override
    public boolean isRunning() {
        log.trace("Pool status check - Is running: {}", isRunning);
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
        log.info("Generating batch of {} tickets for vendor {}", ticketCount, vendorId);
        List<Ticket> tickets = new ArrayList<>();

        try {
            var vendor = userRepository.findById(vendorId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid vendor ID: " + vendorId));

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

                log.debug("Generated ticket - ID: {}, Title: {}, Price: ${}, VendorId: {}",
                        ticket.getId(), ticket.getTitle(), ticket.getPrice(), vendorId);
            }

            log.info("Successfully generated {} tickets for vendor {}", ticketCount, vendorId);
            return tickets;

        } catch (Exception e) {
            log.error("Failed to generate ticket batch - Vendor: {}, Count: {}, Error: {}",
                    vendorId, ticketCount, e.getMessage(), e);
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

            log.debug("Broadcasting pool status update - Status: {}", status);
            messagingTemplate.convertAndSend("/topic/pool-status", status);

        } catch (Exception e) {
            log.error("Failed to broadcast pool status update: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to notify pool status change", e);
        }
    }
}