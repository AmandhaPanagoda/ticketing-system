package com.westminster.ticketing_system.core.threads;

import org.springframework.beans.factory.annotation.Autowired;

import com.westminster.ticketing_system.core.pool.TicketPool;
import com.westminster.ticketing_system.services.systemLog.SystemLogService;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread implementation for ticket vendors.
 * Handles the process of adding tickets to the central ticket pool.
 * Each vendor operates independently and can add a specified number of tickets.
 */
@Slf4j
public class VendorThread extends Thread {

    @Autowired
    private SystemLogService logService;

    private static final String SOURCE = "VendorThread";
    private static final String ORIGINATOR = "SYSTEM";

    private final TicketPool ticketPool;
    private final int vendorId;
    private final int ticketCount;
    private volatile boolean running = true;

    /**
     * Creates a new vendor thread.
     * 
     * @param ticketPool  The shared ticket pool
     * @param vendorId    Unique identifier for the vendor
     * @param ticketCount Number of tickets to add
     */
    public VendorThread(TicketPool ticketPool, int vendorId, int ticketCount) {
        this.ticketPool = ticketPool;
        this.vendorId = vendorId;
        this.ticketCount = ticketCount;
        setName("Vendor-" + vendorId);
    }

    /**
     * Main execution loop for the vendor thread.
     * Attempts to add tickets to the pool until successful or interrupted.
     */
    @Override
    public void run() {
        while (running) {
            try {
                boolean success = ticketPool.addTickets(ticketCount, vendorId);
                if (!success) {
                    logService.warn(SOURCE, "Vendor " + vendorId + " failed to add tickets", ORIGINATOR,
                            "addTickets");
                }
                break;
            } catch (InterruptedException e) {
                logService.info(SOURCE, "Vendor " + vendorId + " thread interrupted", ORIGINATOR, "addTickets");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logService.error(SOURCE, "Error in vendor thread " + vendorId + ": " + e.getMessage(), ORIGINATOR,
                        "addTickets");
                break;
            }
        }
        running = false;
    }

    /**
     * Safely stops the vendor thread
     * Sets running flag to false and interrupts the thread.
     */
    public void shutdown() {
        logService.info(SOURCE, "Shutting down vendor thread", ORIGINATOR, "shutdown");
        running = false;
        interrupt();
    }
}