package com.westminster.ticketing_system.cli.model;

public class Customer implements Runnable {
    private static int customerRetrievalRate;
    private final String name;
    private final TicketPool ticketPool;

    public Customer(String name, TicketPool ticketPool) {
        this.name = name;
        this.ticketPool = ticketPool;
    }

    public static void setCustomerRetrievalRate(int rate) {
        customerRetrievalRate = rate;
    }

    protected String getName() {
        return name;
    }

    protected TicketPool getTicketPool() {
        return ticketPool;
    }

    protected static int getCustomerRetrievalRate() {
        return customerRetrievalRate;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ticketPool.removeTicket(name);
                Thread.sleep(customerRetrievalRate);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}