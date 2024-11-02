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
                ticketPool.addTickets(1);
                Thread.sleep(ticketReleaseRate);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}