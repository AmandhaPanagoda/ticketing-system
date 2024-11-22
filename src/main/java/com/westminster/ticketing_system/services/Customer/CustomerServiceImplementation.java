package com.westminster.ticketing_system.services.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.dtos.TicketSummaryDTO;
import com.westminster.ticketing_system.entity.Ticket;
import com.westminster.ticketing_system.entity.User;
import com.westminster.ticketing_system.repository.TicketRepository;
import com.westminster.ticketing_system.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the CustomerService interface that handles ticket
 * operations for customers.
 */
@Service
@Slf4j
public class CustomerServiceImplementation implements CustomerService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * {@inheritDoc}
     * Retrieves all tickets that are available for purchase.
     * Filters out purchased and deleted tickets.
     */
    @Override
    public List<TicketDTO> getAllAvailableTickets() {
        log.info("Fetching all available tickets");
        try {
            List<Ticket> tickets = ticketRepository.findAll();
            List<TicketDTO> availableTickets = tickets.stream()
                    .filter(ticket -> ticket.getPurchaser() == null)
                    .filter(ticket -> !ticket.isDeletedInd())
                    .map(Ticket::getDto)
                    .collect(Collectors.toList());

            log.info("Retrieved {} available tickets", availableTickets.size());
            return availableTickets;
        } catch (Exception e) {
            log.error("Error while fetching available tickets", e);
            return new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     * Retrieves all non-deleted tickets purchased by the specified customer.
     */
    @Override
    public List<TicketDTO> getCustomerTickets(int userId) {
        log.info("Fetching tickets for customer ID: {}", userId);
        try {
            List<Ticket> tickets = ticketRepository.findByPurchaserId(userId);
            List<TicketDTO> customerTickets = tickets.stream()
                    .filter(ticket -> !ticket.isDeletedInd())
                    .map(Ticket::getDto)
                    .collect(Collectors.toList());

            log.info("Retrieved {} tickets for customer ID: {}", customerTickets.size(), userId);
            return customerTickets;
        } catch (Exception e) {
            log.error("Error while fetching tickets for customer ID: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     * Processes ticket purchase if the ticket is available and the customer exists.
     * Updates purchase timestamp and associates the ticket with the customer.
     */
    @Override
    public boolean purchaseTicket(int ticketId, int userId) {
        log.info("Processing ticket purchase - Ticket ID: {}, Customer ID: {}", ticketId, userId);
        try {
            Optional<Ticket> optionalTicket = ticketRepository.findById(ticketId);
            Optional<User> optionalUser = userRepository.findById(userId);

            if (optionalTicket.isPresent() && optionalUser.isPresent()) {
                Ticket ticket = optionalTicket.get();
                User customer = optionalUser.get();

                // Check if ticket is not already purchased and not deleted
                if (ticket.getPurchaser() == null && !ticket.isDeletedInd()) {
                    ticket.setPurchaser(customer);
                    ticket.setPurchasedDateTime(LocalDateTime.now());
                    ticketRepository.save(ticket);
                    log.info("Successfully purchased ticket ID: {} for customer ID: {}", ticketId, userId);
                    return true;
                } else {
                    log.warn("Ticket ID: {} is not available for purchase", ticketId);
                }
            } else {
                log.warn("Ticket ID: {} or Customer ID: {} not found", ticketId, userId);
            }
            return false;
        } catch (Exception e) {
            log.error("Error while processing ticket purchase - Ticket ID: {}, Customer ID: {}",
                    ticketId, userId, e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * Retrieves ticket summaries for all non-deleted tickets associated with the
     * specified customer.
     * Returns empty list in case of errors or if customer not found.
     */
    @Override
    public List<TicketSummaryDTO> getCustomerTicketSummaries(int userId) {
        log.info("Fetching ticket summaries for customer ID: {}", userId);
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            try {
                List<Ticket> tickets = ticketRepository.findByPurchaserId(userId);
                return tickets.stream()
                        .filter(ticket -> !ticket.isDeletedInd())
                        .map(ticket -> {
                            TicketSummaryDTO summary = new TicketSummaryDTO();
                            summary.setTicketId(ticket.getId());
                            summary.setVendorUsername(ticket.getUser().getUsername());
                            summary.setPurchaserUsername(optionalUser.get().getUsername());
                            summary.setCreatedDateTime(ticket.getCreatedDateTime());
                            summary.setPurchasedDateTime(ticket.getPurchasedDateTime());
                            return summary;
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Error while fetching ticket summaries for customer ID: {}", userId, e);
                return new ArrayList<>();
            }
        }
        log.warn("User not found with ID: {}", userId);
        return new ArrayList<>();
    }
}
