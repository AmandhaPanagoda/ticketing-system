package com.westminster.ticketing_system.core.threads;

import org.springframework.beans.factory.annotation.Autowired;

import com.westminster.ticketing_system.core.pool.TicketPool;
import com.westminster.ticketing_system.services.systemLog.SystemLogService;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread implementation for ticket customers.
 * Manages the ticket purchasing process for individual customers.
 * Each customer attempts to purchase a specific number of tickets.
 */
@Slf4j
public class CustomerThread extends Thread {

    private static final String SOURCE = "CustomerThread";
    private static final String ORIGINATOR = "SYSTEM";

    @Autowired
    private SystemLogService logService;
    private final TicketPool ticketPool;
    private final int customerId;
    private final int ticketCount;
    private volatile boolean running = true;

    /**
     * Creates a new customer thread.
     * 
     * @param ticketPool  The shared ticket pool
     * @param customerId  Unique identifier for the customer
     * @param ticketCount Number of tickets to purchase
     */
    public CustomerThread(TicketPool ticketPool, int customerId, int ticketCount) {
        this.ticketPool = ticketPool;
        this.customerId = customerId;
        this.ticketCount = ticketCount;
        setName("Customer-" + customerId);
    }

    /**
     * Main execution loop for the customer thread.
     * Attempts to purchase tickets from the pool until successful or interrupted.
     */
    @Override
    public void run() {
        while (running) {
            try {
                boolean success = ticketPool.purchaseTickets(ticketCount, customerId);
                if (!success) {
                    logService.warn(SOURCE, "Customer " + customerId + " failed to purchase tickets", ORIGINATOR,
                            "purchaseTickets");
                }
                break;
            } catch (InterruptedException e) {
                logService.info(SOURCE, "Customer " + customerId + " thread interrupted", ORIGINATOR,
                        "purchaseTickets");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logService.error(SOURCE, "Error in customer thread " + customerId + ": " + e.getMessage(), ORIGINATOR,
                        "purchaseTickets");
                break;
            }
        }
        running = false;
    }

    /**
     * Safely stops the customer thread
     * Sets running flag to false and interrupts the thread.
     */
    public void shutdown() {
        logService.info(SOURCE, "Shutting down customer thread", ORIGINATOR, "shutdown");
        running = false;
        interrupt();
    }
}