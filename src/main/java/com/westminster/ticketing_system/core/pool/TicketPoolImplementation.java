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

    @Override
    public void shutdown() {
        isRunning = false;
    }

    @Override
    public int getCurrentTicketCount() {
        return ticketQueue.size();
    }

    @Override
    public boolean isPoolFull() {
        SystemConfigurationDTO config = adminService.getSystemConfiguration();
        return ticketQueue.size() >= config.getMaxTicketCapacity();
    }

    @Override
    public boolean isPoolEmpty() {
        return ticketQueue.isEmpty();
    }

    private List<Ticket> generateTicketBatch(int ticketCount, int vendorId) { // needs to be implemented in a better way
        List<Ticket> tickets = new ArrayList<>();

        for (int i = 0; i < ticketCount; i++) {
            Ticket ticket = new Ticket();

            ticket.setTitle("Test Ticket " + i);
            ticket.setDescription("Test Description for ticket " + i);
            ticket.setUpdatedDateTime(LocalDateTime.now());
            ticket.setUser(userRepository.findById(vendorId).get());
            ticket.setPrice(BigDecimal.valueOf(100.00));
            ticket.setCreatedDateTime(LocalDateTime.now());

            tickets.add(ticket);

            log.debug("Generated ticket: Title={}, Price=${}, VendorId={}",
                    ticket.getTitle(), ticket.getPrice());
        }

        return tickets;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    private void notifyPoolStatusChange() {
        Map<String, Object> status = Map.of(
                "currentTicketCount", getCurrentTicketCount(),
                "isFull", isPoolFull(),
                "isEmpty", isPoolEmpty());
        messagingTemplate.convertAndSend("/topic/pool-status", status);
    }
}