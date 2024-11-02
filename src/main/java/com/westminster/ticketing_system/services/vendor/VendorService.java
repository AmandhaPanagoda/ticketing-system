package com.westminster.ticketing_system.services.vendor;

import java.io.IOException;

import com.westminster.ticketing_system.dtos.TicketDTO;

public interface VendorService {
    boolean addTicket(int userId, TicketDTO ticketDTO) throws IOException;
}
