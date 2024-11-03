package com.westminster.ticketing_system.core.threads;

import com.westminster.ticketing_system.core.pool.TicketPool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomerThread extends Thread {
    private final TicketPool ticketPool;
    private final int customerId;
    private volatile boolean running = true;

    public CustomerThread(TicketPool ticketPool, int customerId) {
        this.ticketPool = ticketPool;
        this.customerId = customerId;
        setName("Customer-" + customerId);
    }

    @Override
    public void run() {
        while (running) {
            try {
                int ticketsWanted = 3; // testing - buy 3 tickets at a time
                boolean success = ticketPool.purchaseTickets(ticketsWanted, customerId);
                if (!success) {
                    log.warn("Customer {} failed to purchase tickets", customerId);
                }
                Thread.sleep(2000); // Simulate customer thinking time
            } catch (InterruptedException e) {
                log.info("Customer {} thread interrupted", customerId);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in customer thread {}: {}", customerId, e.getMessage());
            }
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}