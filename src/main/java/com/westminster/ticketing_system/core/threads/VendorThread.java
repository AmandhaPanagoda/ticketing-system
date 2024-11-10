package com.westminster.ticketing_system.core.threads;

import com.westminster.ticketing_system.core.pool.TicketPool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VendorThread extends Thread {
    private final TicketPool ticketPool;
    private final int vendorId;
    private final int ticketCount;
    private volatile boolean running = true;

    public VendorThread(TicketPool ticketPool, int vendorId, int ticketCount) {
        this.ticketPool = ticketPool;
        this.vendorId = vendorId;
        this.ticketCount = ticketCount;
        setName("Vendor-" + vendorId);
    }

    @Override
    public void run() {
        while (running) {
            try {
                boolean success = ticketPool.addTickets(ticketCount, vendorId);
                if (!success) {
                    log.warn("Vendor {} failed to add tickets", vendorId);
                }
                break;
            } catch (InterruptedException e) {
                log.info("Vendor {} thread interrupted", vendorId);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in vendor thread {}: {}", vendorId, e.getMessage());
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