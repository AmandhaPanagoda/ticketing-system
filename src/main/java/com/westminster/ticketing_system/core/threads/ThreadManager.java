package com.westminster.ticketing_system.core.threads;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.westminster.ticketing_system.core.pool.TicketPool;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ThreadManager {
    private final List<VendorThread> vendorThreads = new ArrayList<>();
    private final List<CustomerThread> customerThreads = new ArrayList<>();
    private final TicketPool ticketPool;
    private volatile boolean systemRunning = false;

    @Autowired
    public ThreadManager(TicketPool ticketPool) {
        this.ticketPool = ticketPool;
    }

    public void addVendorThread(VendorThread vendorThread) {
        if (!systemRunning) {
            throw new IllegalStateException("System is not running");
        }
        vendorThreads.add(vendorThread);
        log.info("Added new vendor thread: {}", vendorThread.getName());
    }

    public void addCustomerThread(CustomerThread customerThread) {
        if (!systemRunning) {
            throw new IllegalStateException("System is not running");
        }
        customerThreads.add(customerThread);
        log.info("Added new customer thread: {}", customerThread.getName());
    }

    public boolean isSystemRunning() {
        return systemRunning;
    }

    public void startSystem() {
        systemRunning = true;
        log.info("System started");
    }

    public void stopSystem() {
        systemRunning = false;
        shutdown();
        log.info("System stopped");
    }

    public void shutdown() {
        vendorThreads.forEach(VendorThread::shutdown);
        customerThreads.forEach(CustomerThread::shutdown);
        ticketPool.shutdown();
    }
}