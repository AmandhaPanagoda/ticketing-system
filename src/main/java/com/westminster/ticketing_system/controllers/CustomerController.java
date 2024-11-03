package com.westminster.ticketing_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.services.Customer.CustomerService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/v1/customer/tickets")
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        return ResponseEntity.ok(customerService.getAllAvailableTickets());
    }

    @GetMapping("/v1/customer/tickets/{userId}")
    public ResponseEntity<List<TicketDTO>> getCustomerTickets(@PathVariable int userId) {
        return ResponseEntity.ok(customerService.getCustomerTickets(userId));
    }

    @PostMapping("/v1/customer/ticket/purchase/{ticketId}/{userId}")
    public ResponseEntity<?> purchaseTicket(@PathVariable int ticketId, @PathVariable int userId) {
        boolean isPurchased = customerService.purchaseTicket(ticketId, userId);
        if (isPurchased) {
            return ResponseEntity.ok("Ticket purchased successfully");
        }
        return ResponseEntity.badRequest().body("Failed to purchase ticket");
    }
}
