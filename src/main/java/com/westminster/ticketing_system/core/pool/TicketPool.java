package com.westminster.ticketing_system.core.pool;

import java.util.List;

import com.westminster.ticketing_system.entity.Ticket;

public interface TicketPool {
    boolean addTickets(List<Ticket> tickets, int vendorId) throws InterruptedException;

    boolean purchaseTickets(int count, int customerId) throws InterruptedException;

    int getCurrentTicketCount();

    boolean isPoolFull();

    boolean isPoolEmpty();

    void shutdown();
}
