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

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Integer ticket = ticketPool.removeTicket();
                if (ticket != null) {
                    System.out.println(name + " : " + "Purchased ticket #" + ticket + "\nRemaining tickets: "
                            + ticketPool.getTicketCount());
                }
                Thread.sleep(customerRetrievalRate);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}