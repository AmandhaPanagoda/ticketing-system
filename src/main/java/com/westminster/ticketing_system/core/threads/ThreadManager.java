package com.westminster.ticketing_system.core.threads;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.westminster.ticketing_system.core.pool.TicketPool;
import com.westminster.ticketing_system.services.systemLog.SystemLogService;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the lifecycle of vendor and customer threads in the ticket system.
 * Handles thread creation, monitoring, and shutdown operations.
 */
@Component
@Slf4j
public class ThreadManager {

    @Autowired
    private SystemLogService logService;

    private static final String SOURCE = "ThreadManager";
    private static final String ORIGINATOR = "SYSTEM";

    private final List<VendorThread> vendorThreads = new ArrayList<>();
    private final List<CustomerThread> customerThreads = new ArrayList<>();
    private final TicketPool ticketPool;
    private volatile boolean systemRunning = false;

    @Autowired
    public ThreadManager(TicketPool ticketPool) {
        this.ticketPool = ticketPool;
    }

    /**
     * Adds a new vendor thread to the system.
     * 
     * @param vendorThread The vendor thread to be added
     * @throws IllegalStateException if system is not running
     */
    public void addVendorThread(VendorThread vendorThread) {
        if (!systemRunning) {
            throw new IllegalStateException("System is not running");
        }
        vendorThreads.add(vendorThread);
        logService.info(SOURCE, "Added new vendor thread: " + vendorThread.getName(), ORIGINATOR, "addVendorThread");
    }

    /**
     * Adds a new customer thread to the system.
     * 
     * @param customerThread The customer thread to be added
     * @throws IllegalStateException if system is not running
     */
    public void addCustomerThread(CustomerThread customerThread) {
        if (!systemRunning) {
            throw new IllegalStateException("System is not running");
        }
        customerThreads.add(customerThread);
        logService.info(SOURCE, "Added new customer thread: " + customerThread.getName(), ORIGINATOR,
                "addCustomerThread");
    }

    /**
     * Checks if the ticket system is currently running.
     * 
     * @return true if the system is running, false otherwise
     */
    public boolean isSystemRunning() {
        logService.info(SOURCE, "System running status: " + systemRunning, ORIGINATOR, "isSystemRunning");
        return systemRunning;
    }

    /**
     * Starts the ticket system and allows threads to begin processing.
     */
    public void startSystem() {
        systemRunning = true;
        logService.info(SOURCE, "System started", ORIGINATOR, "startSystem");
    }

    /**
     * Stops the ticket system and initiates shutdown sequence.
     * This will stop all running threads
     */
    public void stopSystem() {
        systemRunning = false;
        shutdown();
        logService.info(SOURCE, "System stopped", ORIGINATOR, "stopSystem");
    }

    /**
     * Performs a clean shutdown of all system components:
     * - Stops all vendor threads
     * - Stops all customer threads
     * - Shuts down the ticket pool
     */
    public void shutdown() {
        vendorThreads.forEach(VendorThread::shutdown);
        customerThreads.forEach(CustomerThread::shutdown);
        ticketPool.shutdown();
        logService.info(SOURCE, "System shutdown", ORIGINATOR, "shutdown");
    }
}