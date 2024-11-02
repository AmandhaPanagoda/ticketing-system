package com.westminster.ticketing_system.services.vendor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.entity.Ticket;
import com.westminster.ticketing_system.entity.User;
import com.westminster.ticketing_system.repository.TicketRepository;
import com.westminster.ticketing_system.repository.UserRepository;

@Service
public class VendorServiceImplementation implements VendorService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    public boolean addTicket(int userId, TicketDTO ticketDTO) throws IOException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            Ticket ticket = new Ticket();
            ticket.setDescription(ticketDTO.getDescription());
            ticket.setPrice(ticketDTO.getPrice());
            ticket.setTitle(ticketDTO.getTitle());
            if (ticketDTO.getImage() != null && !ticketDTO.getImage().isEmpty()) {
                ticket.setImage(ticketDTO.getImage().getBytes());
            }
            ticket.setUser(optionalUser.get());

            ticketRepository.save(ticket);
            return true;
        }
        return false;
    }

    @Override
    public List<TicketDTO> getVendorTickets(int userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            List<Ticket> tickets = ticketRepository.findByUserId(userId);
            return tickets.stream()
                    .map(Ticket::getDto)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public boolean updateTicket(int ticketId, TicketDTO ticketDTO) throws IOException {
        Optional<Ticket> optionalTicket = ticketRepository.findById(ticketId);
        if (optionalTicket.isPresent() && !optionalTicket.get().isDeletedInd()) {
            Ticket ticket = optionalTicket.get();
            ticket.setTitle(ticketDTO.getTitle());
            ticket.setPrice(ticketDTO.getPrice());
            ticket.setDescription(ticketDTO.getDescription());
            if (ticketDTO.getImage() != null) {
                ticket.setImage(ticketDTO.getImage().getBytes());
            }
            ticketRepository.save(ticket);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteTicket(int ticketId, int userId) {
        Optional<Ticket> optionalTicket = ticketRepository.findById(ticketId);
        if (optionalTicket.isPresent() &&
                optionalTicket.get().getUser().getId() == userId &&
                !optionalTicket.get().isDeletedInd()) {
            Ticket ticket = optionalTicket.get();
            ticket.setDeletedInd(true);
            ticketRepository.save(ticket);
            return true;
        }
        return false;
    }
}
