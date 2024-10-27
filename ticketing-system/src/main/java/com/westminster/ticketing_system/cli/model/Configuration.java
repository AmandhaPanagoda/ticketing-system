package com.westminster.ticketing_system.cli.model;

public class Configuration {
    private int totalTickets;
    private int maxTicketCapacity;
    private int ticketReleaseRate;
    private int customerRetrievalRate;

    public void configure(int totalTickets, int maxTicketCapacity, int ticketReleaseRate, int customerRetrievalRate) {
        this.totalTickets = totalTickets;
        this.maxTicketCapacity = maxTicketCapacity;
        this.ticketReleaseRate = ticketReleaseRate;
        this.customerRetrievalRate = customerRetrievalRate;
    }

    public void applyConfiguration(TicketPool ticketPool) {
        ticketPool.setMaxTicketCapacity(maxTicketCapacity);
        ticketPool.addTickets(totalTickets);
        Vendor.setTicketReleaseRate(ticketReleaseRate);
        Customer.setCustomerRetrievalRate(customerRetrievalRate);
    }
}
