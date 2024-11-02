package com.westminster.ticketing_system.services.Customer;

import java.util.List;

import com.westminster.ticketing_system.dtos.TicketDTO;

public interface CustomerService {
    List<TicketDTO> getAllAvailableTickets();

    List<TicketDTO> getCustomerTickets(int userId);

    boolean purchaseTicket(int ticketId, int userId);
}