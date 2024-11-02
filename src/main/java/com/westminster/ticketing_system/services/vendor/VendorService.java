package com.westminster.ticketing_system.services.vendor;

import java.io.IOException;
import java.util.List;

import com.westminster.ticketing_system.dtos.TicketDTO;

public interface VendorService {
    boolean addTicket(int userId, TicketDTO ticketDTO) throws IOException;

    List<TicketDTO> getVendorTickets(int userId);

    boolean updateTicket(int ticketId, TicketDTO ticketDTO) throws IOException;

    boolean deleteTicket(int ticketId, int userId);
}
