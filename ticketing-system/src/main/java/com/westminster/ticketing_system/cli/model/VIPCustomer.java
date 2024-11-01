package com.westminster.ticketing_system.cli.model;

public class VIPCustomer extends Customer {
    public VIPCustomer(String name, TicketPool ticketPool) {
        super(name, ticketPool);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                getTicketPool().removeVIPTicket(getName());
                Thread.sleep(getCustomerRetrievalRate());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}