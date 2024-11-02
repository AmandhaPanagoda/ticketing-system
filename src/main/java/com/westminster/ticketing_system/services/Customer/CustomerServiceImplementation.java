package com.westminster.ticketing_system.services.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.entity.Ticket;
import com.westminster.ticketing_system.entity.User;
import com.westminster.ticketing_system.repository.TicketRepository;
import com.westminster.ticketing_system.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImplementation implements CustomerService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<TicketDTO> getAllAvailableTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream()
                .filter(ticket -> ticket.getPurchaser() == null)
                .filter(ticket -> !ticket.isDeletedInd())
                .map(Ticket::getDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getCustomerTickets(int userId) {
        List<Ticket> tickets = ticketRepository.findByPurchaserId(userId);
        return tickets.stream()
                .filter(ticket -> !ticket.isDeletedInd())
                .map(Ticket::getDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean purchaseTicket(int ticketId, int userId) {
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
                return true;
            }
        }
        return false;
    }
}
