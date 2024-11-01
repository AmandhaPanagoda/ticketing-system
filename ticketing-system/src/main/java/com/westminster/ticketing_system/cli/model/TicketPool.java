package com.westminster.ticketing_system.cli.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TicketPool {
    private final List<Integer> tickets;
    private int maxTicketCapacity;
    private final Object lockObject = new Object();
    private int TicketId = 1;

    public TicketPool() {
        this.tickets = Collections.synchronizedList(new LinkedList<>());
    }

    public synchronized void setMaxTicketCapacity(int maxTicketCapacity) {
        this.maxTicketCapacity = maxTicketCapacity;
    }

    public synchronized boolean addTickets(int count) {
        if (tickets.size() + count <= maxTicketCapacity) {
            for (int i = 0; i < count; i++) {
                tickets.add(TicketId++);
                System.out.println("Ticket #" + (TicketId - 1) + " added. Current total: " + tickets.size());
            }
            return true;
        }
        return false;
    }

    public Integer removeVIPTicket(String customerName) {
        synchronized (lockObject) {
            if (!tickets.isEmpty()) {
                Integer ticket = tickets.remove(0);
                System.out.println(customerName + "(VIP) : Purchased ticket #" + ticket +
                        "\nRemaining tickets: " + tickets.size());
                return ticket;
            }
            return null;
        }
    }

    public Integer removeTicket(String customerName) {
        synchronized (lockObject) {
            if (!tickets.isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (!tickets.isEmpty()) {
                    Integer ticket = tickets.remove(0);
                    System.out.println(customerName + " : Purchased ticket #" + ticket +
                            "\nRemaining tickets: " + tickets.size());
                    return ticket;
                }
            }
            return null;
        }
    }

    public synchronized int getTicketCount() {
        return tickets.size();
    }
}