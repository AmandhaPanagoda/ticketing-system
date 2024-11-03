package com.westminster.ticketing_system.core.threads;

import com.westminster.ticketing_system.core.pool.TicketPool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomerThread extends Thread {
    private final TicketPool ticketPool;
    private final int customerId;
    private final int ticketCount;
    private volatile boolean running = true;

    public CustomerThread(TicketPool ticketPool, int customerId, int ticketCount) {
        this.ticketPool = ticketPool;
        this.customerId = customerId;
        this.ticketCount = ticketCount;
        setName("Customer-" + customerId);
    }

    @Override
    public void run() {
        while (running) {
            try {
                boolean success = ticketPool.purchaseTickets(ticketCount, customerId);
                if (!success) {
                    log.warn("Customer {} failed to purchase tickets", customerId);
                }
                break;
            } catch (InterruptedException e) {
                log.info("Customer {} thread interrupted", customerId);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in customer thread {}: {}", customerId, e.getMessage());
                break;
            }
        }
        running = false;
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}