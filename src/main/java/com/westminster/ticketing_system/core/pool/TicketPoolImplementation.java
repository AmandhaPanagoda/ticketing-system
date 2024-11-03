package com.westminster.ticketing_system.core.pool;

import com.westminster.ticketing_system.entity.Ticket;
import com.westminster.ticketing_system.repository.TicketRepository;
import com.westminster.ticketing_system.repository.UserRepository;
import com.westminster.ticketing_system.services.admin.AdminService;
import com.westminster.ticketing_system.dtos.SystemConfigurationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TicketPoolImplementation implements TicketPool {
    private final ConcurrentLinkedQueue<Ticket> ticketQueue;
    private final Semaphore availableTickets;
    private final Semaphore capacityControl;
    private final Object lock = new Object();
    private volatile boolean isRunning = true;

    @Autowired
    private AdminService adminService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    public TicketPoolImplementation() {
        SystemConfigurationDTO config = adminService.getSystemConfiguration();
        this.ticketQueue = new ConcurrentLinkedQueue<>();
        this.availableTickets = new Semaphore(0, true);
        this.capacityControl = new Semaphore(config.getMaxTicketCapacity(), true);
    }

    @Override
    public boolean addTickets(List<Ticket> tickets, int vendorId) throws InterruptedException {
        if (!isRunning)
            return false;

        SystemConfigurationDTO config = adminService.getSystemConfiguration();
        long releaseRate = config.getTicketReleaseRate();

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
                        ticket.setPurchaser(userRepository.findById(customerId).get());
                        ticket.setPurchasedDateTime(LocalDateTime.now());
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
}