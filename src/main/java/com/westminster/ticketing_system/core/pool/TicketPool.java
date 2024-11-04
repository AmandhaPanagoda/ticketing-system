package com.westminster.ticketing_system.core.pool;

public interface TicketPool {
    boolean addTickets(int ticketCount, int vendorId) throws InterruptedException;

    boolean purchaseTickets(int count, int customerId) throws InterruptedException;

    int getCurrentTicketCount();

    boolean isPoolFull();

    boolean isPoolEmpty();

    void shutdown();
}
