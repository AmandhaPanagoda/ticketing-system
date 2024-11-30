package com.westminster.ticketing_system.services.vendor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.dtos.TicketSummaryDTO;
import com.westminster.ticketing_system.entity.Ticket;
import com.westminster.ticketing_system.entity.User;
import com.westminster.ticketing_system.repository.TicketRepository;
import com.westminster.ticketing_system.repository.UserRepository;
import com.westminster.ticketing_system.services.systemLog.SystemLogService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the VendorService interface that handles ticket operations
 * for vendors.
 */
@Service
@Slf4j
public class VendorServiceImplementation implements VendorService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SystemLogService logService;

    private final String source = "VendorService";
    private final String originator = "SYSTEM";

    /**
     * {@inheritDoc}
     * Creates a new ticket with the provided details and associates it with the
     * specified vendor.
     * Handles image processing and sets creation/update timestamps.
     */
    @Override
    public boolean addTicket(int userId, TicketDTO ticketDTO) throws IOException {
        logService.info(source, "Adding new ticket for user ID: " + userId, String.valueOf(userId), "addTicket");
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            try {
                Ticket ticket = new Ticket();
                ticket.setDescription(ticketDTO.getDescription());
                ticket.setPrice(ticketDTO.getPrice());
                ticket.setTitle(ticketDTO.getTitle());
                if (ticketDTO.getImage() != null && !ticketDTO.getImage().isEmpty()) {
                    ticket.setImage(ticketDTO.getImage().getBytes());
                }
                ticket.setCreatedDateTime(LocalDateTime.now());
                ticket.setUpdatedDateTime(LocalDateTime.now());
                ticket.setUser(optionalUser.get());

                ticketRepository.save(ticket);
                logService.info(source, "Successfully added ticket for user ID: " + userId, String.valueOf(userId),
                        "addTicket");
                return true;
            } catch (Exception e) {
                logService.error(source, "Error while adding ticket for user ID: " + userId, String.valueOf(userId),
                        "addTicket");
                return false;
            }
        }

        logService.warn(source, "User not found with ID: " + userId, String.valueOf(userId), "addTicket");

        return false;
    }

    /**
     * {@inheritDoc}
     * Retrieves all non-deleted tickets associated with the specified vendor.
     * Returns empty list in case of errors or if vendor not found.
     */
    @Override
    public List<TicketDTO> getVendorTickets(int userId) {
        logService.info(source, "Fetching tickets for vendor ID: " + userId, String.valueOf(userId),
                "getVendorTickets");
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            try {
                List<Ticket> tickets = ticketRepository.findByUserId(userId);
                List<TicketDTO> ticketDTOs = tickets.stream()
                        .filter(ticket -> !ticket.isDeletedInd())
                        .map(Ticket::getDto)
                        .collect(Collectors.toList());
                logService.info(source, "Retrieved " + ticketDTOs.size() + " tickets for vendor ID: " + userId,
                        String.valueOf(userId), "getVendorTickets");
                return ticketDTOs;
            } catch (Exception e) {
                logService.error(source, "Error while fetching tickets for vendor ID: " + userId,
                        String.valueOf(userId), "getVendorTickets");
                return new ArrayList<>();
            }
        }

        logService.warn(source, "User not found with ID: " + userId, String.valueOf(userId), "getVendorTickets");

        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     * Updates an existing ticket with new information if the ticket exists and
     * isn't deleted.
     * Handles image updates and sets update timestamp.
     */
    @Override
    public boolean updateTicket(int ticketId, TicketDTO ticketDTO) throws IOException {
        logService.info(source, "Updating ticket ID: " + ticketId, originator, "updateTicket");
        Optional<Ticket> optionalTicket = ticketRepository.findById(ticketId);

        if (optionalTicket.isPresent() && !optionalTicket.get().isDeletedInd()) {
            try {
                Ticket ticket = optionalTicket.get();
                ticket.setTitle(ticketDTO.getTitle());
                ticket.setPrice(ticketDTO.getPrice());
                ticket.setDescription(ticketDTO.getDescription());
                if (ticketDTO.getImage() != null) {
                    ticket.setImage(ticketDTO.getImage().getBytes());
                }
                ticket.setUpdatedDateTime(LocalDateTime.now());
                ticketRepository.save(ticket);
                logService.info(source, "Successfully updated ticket ID: " + ticketId, originator,
                        "updateTicket");
                return true;
            } catch (Exception e) {
                logService.error(source, "Error while updating ticket ID: " + ticketId, originator,
                        "updateTicket");
                return false;
            }
        }

        logService.warn(source, "Ticket not found or already deleted with ID: " + ticketId, originator, "updateTicket");

        return false;
    }

    /**
     * {@inheritDoc}
     * Performs a soft delete on the specified ticket if it belongs to the vendor
     * and isn't already deleted.
     * Updates the deletion status and timestamp.
     */
    @Override
    public boolean deleteTicket(int ticketId, int userId) {
        logService.info(source, "Deleting ticket ID: " + ticketId + " for user ID: " + userId, originator,
                "deleteTicket");
        Optional<Ticket> optionalTicket = ticketRepository.findById(ticketId);

        if (optionalTicket.isPresent() &&
                optionalTicket.get().getUser().getId() == userId &&
                !optionalTicket.get().isDeletedInd()) {
            try {
                Ticket ticket = optionalTicket.get();
                ticket.setDeletedInd(true);
                ticket.setUpdatedDateTime(LocalDateTime.now());
                ticketRepository.save(ticket);
                logService.info(source, "Successfully deleted ticket ID: " + ticketId, originator, "deleteTicket");
                return true;
            } catch (Exception e) {
                logService.error(source, "Error while deleting ticket ID: " + ticketId, originator, "deleteTicket");
                return false;
            }
        }

        logService.warn(source, "Ticket not found, already deleted, or unauthorized access for ticket ID: " + ticketId,
                originator, "deleteTicket");

        return false;
    }

    /**
     * {@inheritDoc}
     * Retrieves ticket summaries for all non-deleted tickets associated with the
     * specified vendor.
     * Returns empty list in case of errors or if vendor not found.
     */
    @Override
    public List<TicketSummaryDTO> getVendorTicketSummaries(int userId) {
        logService.info(source, "Fetching ticket summaries for vendor ID: " + userId, originator,
                "getVendorTicketSummaries");
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            try {
                List<Ticket> tickets = ticketRepository.findByUserId(userId);
                return tickets.stream()
                        .filter(ticket -> !ticket.isDeletedInd())
                        .map(ticket -> {
                            TicketSummaryDTO summary = new TicketSummaryDTO();
                            summary.setTicketId(ticket.getId());
                            summary.setVendorUsername(ticket.getUser().getUsername());
                            summary.setPurchaserUsername(
                                    ticket.getPurchaser() != null ? ticket.getPurchaser().getUsername() : null);
                            summary.setCreatedDateTime(ticket.getCreatedDateTime());
                            summary.setPurchasedDateTime(ticket.getPurchasedDateTime());
                            return summary;
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
                logService.error(source, "Error while fetching ticket summaries for vendor ID: " + userId, originator,
                        "getVendorTicketSummaries");
                return new ArrayList<>();
            }
        }

        logService.warn(source, "User not found with ID: " + userId, originator, "getVendorTicketSummaries");

        return new ArrayList<>();
    }
}
