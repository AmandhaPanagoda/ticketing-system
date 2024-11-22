package com.westminster.ticketing_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.services.customer.CustomerService;

import java.util.List;

/**
 * @deprecated This is a legacy V1 controller for customer operations.
 *             This version is maintained for backward compatibility but is no
 *             longer actively used.
 *             Please refer to V2 API endpoints for new implementations.
 */
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        return ResponseEntity.ok(customerService.getAllAvailableTickets());
    }

    @GetMapping("/tickets/{userId}")
    public ResponseEntity<List<TicketDTO>> getCustomerTickets(@PathVariable int userId) {
        return ResponseEntity.ok(customerService.getCustomerTickets(userId));
    }

    @PostMapping("/ticket/purchase/{ticketId}/{userId}")
    public ResponseEntity<?> purchaseTicket(@PathVariable int ticketId, @PathVariable int userId) {
        boolean isPurchased = customerService.purchaseTicket(ticketId, userId);
        if (isPurchased) {
            return ResponseEntity.ok("Ticket purchased successfully");
        }
        return ResponseEntity.badRequest().body("Failed to purchase ticket");
    }
}
