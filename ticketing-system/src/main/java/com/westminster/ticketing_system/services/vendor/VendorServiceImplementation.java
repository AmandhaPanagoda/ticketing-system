package com.westminster.ticketing_system.services.vendor;

import java.io.IOException;
import java.util.Optional;

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
            ticket.setImage(ticketDTO.getImage().getBytes());
            ticket.setUser(optionalUser.get());

            ticketRepository.save(ticket);
            return true;
        }
        return false;
    }
}
