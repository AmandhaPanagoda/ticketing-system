package com.westminster.ticketing_system.core.pool;

/**
 * This interface provides operations for managing tickets in a concurrent
 * environment,
 * supporting both ticket vendors and customers.
 */
public interface TicketPool {
    boolean addTickets(int ticketCount, int vendorId) throws InterruptedException;

    boolean purchaseTickets(int count, int customerId) throws InterruptedException;

    int getCurrentTicketCount();

    boolean isPoolFull();

    boolean isPoolEmpty();

    boolean isRunning();

    void shutdown();
}
