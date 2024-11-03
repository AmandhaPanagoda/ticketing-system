package com.westminster.ticketing_system.core.threads;

import com.westminster.ticketing_system.core.pool.TicketPool;
import com.westminster.ticketing_system.entity.Ticket;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
public class VendorThread extends Thread {
    private final TicketPool ticketPool;
    private final int vendorId;
    private final int batchSize;
    private volatile boolean running = true;

    public VendorThread(TicketPool ticketPool, int vendorId, int batchSize) {
        this.ticketPool = ticketPool;
        this.vendorId = vendorId;
        this.batchSize = batchSize;
        setName("Vendor-" + vendorId);
    }

    @Override
    public void run() {
        while (running) {
            try {
                List<Ticket> tickets = generateTicketBatch();
                boolean success = ticketPool.addTickets(tickets, vendorId);
                if (!success) {
                    log.warn("Vendor {} failed to add tickets", vendorId);
                }
                Thread.sleep(1000); // Prevent tight loop
            } catch (InterruptedException e) {
                log.info("Vendor {} thread interrupted", vendorId);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in vendor thread {}: {}", vendorId, e.getMessage());
            }
        }
    }

    private List<Ticket> generateTicketBatch() {
        List<Ticket> tickets = new ArrayList<>();

        for (int i = 0; i < batchSize; i++) {
            Ticket ticket = new Ticket();

            ticket.setTitle("Test Ticket " + i);
            ticket.setDescription("Test Description for ticket " + i);
            ticket.setPrice(BigDecimal.valueOf(100.00));
            ticket.setCreatedDateTime(LocalDateTime.now());

            tickets.add(ticket);

            log.debug("Generated ticket: Title={}, Price=${}, VendorId={}",
                    ticket.getTitle(), ticket.getPrice());
        }

        return tickets;
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}