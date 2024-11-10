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

    @Autowired
    public ThreadManager(TicketPool ticketPool) {
        this.ticketPool = ticketPool;
    }

    public void addVendorThread(VendorThread vendorThread) {
        vendorThreads.add(vendorThread);
        log.info("Added new vendor thread: {}", vendorThread.getName());
    }

    public void addCustomerThread(CustomerThread customerThread) {
        customerThreads.add(customerThread);
        log.info("Added new customer thread: {}", customerThread.getName());
    }

    // for simulation, pass in vendorCount and customerCount
    // this method is not intended to be used, this will set false values in the
    // purchaser and user id fields
    public void startSystem(int vendorCount, int customerCount) {
        // Start vendor threads
        for (int i = 1; i <= vendorCount; i++) {
            VendorThread vendorThread = new VendorThread(ticketPool, i, 5);
            vendorThreads.add(vendorThread);
            vendorThread.start();
        }

        // Start customer threads
        for (int i = 1; i <= customerCount; i++) {
            CustomerThread customerThread = new CustomerThread(ticketPool, i, 6);
            customerThreads.add(customerThread);
            customerThread.start();
        }
    }

    public void shutdown() {
        vendorThreads.forEach(VendorThread::shutdown);
        customerThreads.forEach(CustomerThread::shutdown);
        ticketPool.shutdown();
    }
}