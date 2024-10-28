package com.westminster.ticketing_system.cli.model;

public class Vendor implements Runnable {
    private static int ticketReleaseRate;
    private final String name;
    private final TicketPool ticketPool;

    public Vendor(String name, TicketPool ticketPool) {
        this.name = name;
        this.ticketPool = ticketPool;
    }

    public static void setTicketReleaseRate(int rate) {
        ticketReleaseRate = rate;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (ticketPool.addTickets(1)) {
                    System.out.println(name + " : " + "Ticket added. \nCurrent total: " + ticketPool.getTicketCount());
                }
                Thread.sleep(ticketReleaseRate);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}