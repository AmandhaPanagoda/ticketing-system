package com.westminster.ticketing_system.services.vendor;

import java.io.IOException;
import java.util.List;

import com.westminster.ticketing_system.dtos.TicketDTO;

/**
 * Service interface for managing vendor-related ticket operations.
 */
public interface VendorService {
    /**
     * Adds a new ticket for a specific vendor.
     *
     * @param userId    The ID of the vendor creating the ticket
     * @param ticketDTO The ticket data to be added
     * @return true if ticket was successfully added, false otherwise
     * @throws IOException if there's an error processing the ticket image
     */
    boolean addTicket(int userId, TicketDTO ticketDTO) throws IOException;

    /**
     * Retrieves all active tickets for a specific vendor.
     *
     * @param userId The ID of the vendor
     * @return List of tickets associated with the vendor, empty list if none found
     */
    List<TicketDTO> getVendorTickets(int userId);

    /**
     * Updates an existing ticket.
     *
     * @param ticketId  The ID of the ticket to update
     * @param ticketDTO The updated ticket data
     * @return true if ticket was successfully updated, false otherwise
     * @throws IOException if there's an error processing the ticket image
     */
    boolean updateTicket(int ticketId, TicketDTO ticketDTO) throws IOException;

    /**
     * Soft deletes a ticket for a specific vendor.
     *
     * @param ticketId The ID of the ticket to delete
     * @param userId   The ID of the vendor who owns the ticket
     * @return true if ticket was successfully deleted, false otherwise
     */
    boolean deleteTicket(int ticketId, int userId);
}
