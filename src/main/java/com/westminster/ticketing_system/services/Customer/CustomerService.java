package com.westminster.ticketing_system.services.customer;

import java.util.List;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.dtos.TicketSummaryDTO;

/**
 * Service interface for managing customer-related ticket operations.
 */
public interface CustomerService {
    /**
     * Retrieves all available tickets that haven't been purchased or deleted.
     *
     * @return List of available tickets, empty list if none found
     */
    List<TicketDTO> getAllAvailableTickets();

    /**
     * Retrieves all tickets purchased by a specific customer.
     *
     * @param userId The ID of the customer
     * @return List of tickets purchased by the customer, empty list if none found
     */
    List<TicketDTO> getCustomerTickets(int userId);

    /**
     * Processes the purchase of a ticket by a customer.
     *
     * @param ticketId The ID of the ticket to purchase
     * @param userId   The ID of the customer making the purchase
     * @return true if purchase was successful, false otherwise
     */
    boolean purchaseTicket(int ticketId, int userId);

    /**
     * Retrieves ticket summaries for all active tickets for a specific customer.
     *
     * @param userId The ID of the customer
     * @return List of ticket summaries associated with the customer, empty list if
     *         none found
     */
    List<TicketSummaryDTO> getCustomerTicketSummaries(int userId);
}