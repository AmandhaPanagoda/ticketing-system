package com.westminster.ticketing_system.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.services.vendor.VendorService;

@RestController
@RequestMapping("/api/v1/vendor")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @PostMapping("/ticket/{userId}")
    public ResponseEntity<?> addTicket(@PathVariable int userId, @ModelAttribute TicketDTO ticketDTO)
            throws IOException {
        boolean isAdded = vendorService.addTicket(userId, ticketDTO);
        if (isAdded) {
            return ResponseEntity.ok("Ticket added successfully");
        }
        return ResponseEntity.badRequest().body("Failed to add ticket");
    }

}
