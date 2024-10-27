package com.westminster.ticketing_system.cli.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TicketPool {
    private final List<Integer> tickets;
    private int maxTicketCapacity;

    public TicketPool() {
        this.tickets = Collections.synchronizedList(new LinkedList<>());
    }

    public synchronized void setMaxTicketCapacity(int maxTicketCapacity) {
        this.maxTicketCapacity = maxTicketCapacity;
    }

    public synchronized boolean addTickets(int count) {
        if (tickets.size() + count <= maxTicketCapacity) {
            for (int i = 0; i < count; i++) {
                tickets.add(tickets.size() + 1);
            }
            System.out.println(count + " tickets added. Total: " + tickets.size());
            return true;
        }
        return false;
    }

    public synchronized Integer removeTicket() {
        if (!tickets.isEmpty()) {
            Integer ticket = tickets.remove(0);
            System.out.println("Ticket " + ticket + " sold. Remaining: " + tickets.size());
            return ticket;
        }
        return null;
    }

    public synchronized int getTicketCount() {
        return tickets.size();
    }
}