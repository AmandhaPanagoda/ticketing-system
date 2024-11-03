package com.westminster.ticketing_system.core.pool;

import com.westminster.ticketing_system.entity.Ticket;
import com.westminster.ticketing_system.repository.TicketRepository;
import com.westminster.ticketing_system.repository.UserRepository;
import com.westminster.ticketing_system.services.admin.AdminService;
import com.westminster.ticketing_system.dtos.SystemConfigurationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TicketPoolImplementation implements TicketPool {
    private final AdminService adminService;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final int maxPoolSize;
    private final ConcurrentLinkedQueue<Ticket> ticketQueue;
    private final Semaphore availableTickets;
    private final Semaphore capacityControl;
    private final Object lock = new Object();
    private volatile boolean isRunning = true;

    @Autowired
    public TicketPoolImplementation(AdminService adminService, TicketRepository ticketRepository,
            UserRepository userRepository) {
        this.adminService = adminService;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;

        // Determine maxPoolSize before using it
        int poolSize = 10; // Default value
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

    @Override
    public boolean addTickets(int ticketCount, int vendorId) throws InterruptedException {
        if (!isRunning)
            return false;

        // Calculate available capacity
        int currentCount = getCurrentTicketCount();
        int availableCapacity = maxPoolSize - currentCount;

        if (availableCapacity <= 0) {
            log.warn("Vendor {} cannot add tickets - pool is full (capacity: {})",
                    vendorId, maxPoolSize);
            return false;
        }

        // Adjust ticketCount to available capacity
        int adjustedTicketCount = Math.min(ticketCount, availableCapacity);
        if (adjustedTicketCount < ticketCount) {
            log.info("Vendor {} requested {} tickets, but only {} slots available. Adding partial batch.",
                    vendorId, ticketCount, adjustedTicketCount);
        }

        SystemConfigurationDTO config = adminService.getSystemConfiguration();
        long releaseRate = config.getTicketReleaseRate();
        List<Ticket> tickets = generateTicketBatch(adjustedTicketCount, vendorId); // generate tickets for the adjusted
                                                                                   // ticket count

        for (Ticket ticket : tickets) {
            // Wait for release rate
            Thread.sleep(releaseRate);

            // Try to acquire capacity permit
            if (!capacityControl.tryAcquire(5, TimeUnit.SECONDS)) {
                log.warn("Vendor {} timeout waiting for capacity", vendorId);
                return false;
            }

            try {
                synchronized (lock) {
                    // Save to database
                    Ticket savedTicket = ticketRepository.save(ticket);
                    ticketQueue.offer(savedTicket);
                    availableTickets.release();
                    log.info("Vendor {} added ticket {}. Current pool size: {}",
                            vendorId, savedTicket.getId(), ticketQueue.size());
                }
            } catch (Exception e) {
                capacityControl.release();
                throw e;
            }
        }
        return true;
    }

    @Override
    public boolean purchaseTickets(int count, int customerId) throws InterruptedException {
        if (!isRunning)
            return false;

        SystemConfigurationDTO config = adminService.getSystemConfiguration();
        long retrievalRate = config.getCustomerRetrievalRate();
        List<Ticket> purchasedTickets = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Wait for retrieval rate
            Thread.sleep(retrievalRate);

            // Try to acquire ticket permit
            if (!availableTickets.tryAcquire(5, TimeUnit.SECONDS)) {
                log.warn("Customer {} timeout waiting for ticket", customerId);
                return false;
            }

            try {
                synchronized (lock) {
                    Ticket ticket = ticketQueue.poll();
                    if (ticket != null) {
                        // ticket.setPurchaser(userRepository.findById(customerId).get());
                        // ticket.setPurchasedDateTime(LocalDateTime.now());
                        purchasedTickets.add(ticket);
                        capacityControl.release();
                    }
                }
            } catch (Exception e) {
                availableTickets.release();
                throw e;
            }
        }

        // save purchased tickets
        // ticketRepository.saveAll(purchasedTickets);
        log.info("Customer {} purchased {} tickets", customerId, count);
        return true;
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
}