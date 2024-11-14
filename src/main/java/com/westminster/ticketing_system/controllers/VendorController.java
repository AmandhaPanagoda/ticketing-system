package com.westminster.ticketing_system.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.services.vendor.VendorService;

import lombok.extern.slf4j.Slf4j;

/**
 * @deprecated This is a legacy V1 controller for vendor operations.
 *             This version is maintained for backward compatibility but is no
 *             longer actively used.
 *             Please refer to V2 API endpoints for new implementations.
 */
@RestController
@RequestMapping("/api/v1/vendor")
@Slf4j
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @PostMapping("/ticket/{userId}")
    public ResponseEntity<?> addTicket(@PathVariable int userId, @RequestBody TicketDTO ticketDTO)
            throws IOException {
        boolean isAdded = vendorService.addTicket(userId, ticketDTO);
        if (isAdded) {
            return ResponseEntity.ok("Ticket added successfully");
        }
        return ResponseEntity.badRequest().body("Failed to add ticket");
    }

    @GetMapping("/tickets/{userId}")
    public ResponseEntity<?> getVendorTickets(@PathVariable int userId) {
        List<TicketDTO> tickets = vendorService.getVendorTickets(userId);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/ticket/{ticketId}")
    public ResponseEntity<?> updateTicket(@PathVariable int ticketId, @RequestBody TicketDTO ticketDTO)
            throws IOException {
        boolean isUpdated = vendorService.updateTicket(ticketId, ticketDTO);
        if (isUpdated) {
            return ResponseEntity.ok("Ticket updated successfully");
        }
        return ResponseEntity.badRequest().body("Failed to update ticket");
    }

    @DeleteMapping("/ticket/{ticketId}/{userId}")
    public ResponseEntity<?> deleteTicket(@PathVariable int ticketId, @PathVariable int userId) {
        boolean isDeleted = vendorService.deleteTicket(ticketId, userId);
        if (isDeleted) {
            return ResponseEntity.ok("Ticket deleted successfully");
        }
        return ResponseEntity.badRequest().body("Failed to delete ticket");
    }
}
