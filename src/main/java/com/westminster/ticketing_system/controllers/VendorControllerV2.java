package com.westminster.ticketing_system.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.core.threads.ThreadManager;
import com.westminster.ticketing_system.core.threads.VendorThread;
import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.core.pool.TicketPool;
import com.westminster.ticketing_system.services.vendor.VendorService;
import com.westminster.ticketing_system.dtos.TicketSummaryDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller handling vendor operations for ticket management (V2)
 */
@RestController
@RequestMapping("/api/v2/vendor")
@Slf4j
public class VendorControllerV2 {

    @Autowired
    private ThreadManager threadManager;

    @Autowired
    private TicketPool ticketPool;

    @Autowired
    private VendorService vendorService;

    /**
     * Asynchronously processes ticket addition requests from vendors
     * 
     * @param userId      The vendor's unique identifier
     * @param ticketCount Number of tickets to add
     * @return ResponseEntity with processing status
     */
    @PostMapping("/tickets/{ticketCount}")
    public ResponseEntity<?> addTicketsV2(
            @RequestHeader("Userid") int userId,
            @PathVariable int ticketCount) {
        log.info("Received request to add {} tickets from vendor {}", ticketCount, userId);

        try {
            if (!threadManager.isSystemRunning()) {
                log.warn("System not running - rejected request from vendor {}", userId);
                return ResponseEntity.badRequest().body("System is currently not running");
            }

            VendorThread vendorThread = new VendorThread(ticketPool, userId, ticketCount);
            threadManager.addVendorThread(vendorThread);
            vendorThread.start();

            log.info("Successfully initiated ticket addition process for vendor {}", userId);
            return ResponseEntity.accepted()
                    .body(String.format("Processing request to add %d tickets", ticketCount));

        } catch (IllegalStateException e) {
            log.error("Invalid state for vendor {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments from vendor {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Invalid request parameters");
        } catch (Exception e) {
            log.error("Unexpected error processing vendor {} request: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred while processing your request");
        }
    }

    /**
     * Retrieves current status of the ticket pool
     */
    @GetMapping("/pool/status")
    public ResponseEntity<?> getPoolStatus(@RequestHeader("Userid") int userId) {
        log.debug("Pool status requested by vendor {}", userId);
        try {
            return ResponseEntity.ok(Map.of(
                    "currentTicketCount", ticketPool.getCurrentTicketCount(),
                    "isFull", ticketPool.isPoolFull(),
                    "isEmpty", ticketPool.isPoolEmpty(),
                    "isRunning", ticketPool.isRunning()));
        } catch (Exception e) {
            log.error("Error retrieving pool status for vendor {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to retrieve pool status");
        }
    }

    /**
     * Retrieves all tickets for a specific vendor
     */
    @GetMapping("/tickets")
    public ResponseEntity<?> getVendorTickets(@RequestHeader("Userid") int userId) {
        try {
            log.info("Fetching ticket summaries for vendor with ID: {}", userId);
            List<TicketSummaryDTO> tickets = vendorService.getVendorTicketSummaries(userId);
            log.info("Successfully retrieved {} ticket summaries for vendor ID: {}", tickets.size(), userId);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("Error fetching ticket summaries for vendor ID: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred while processing your request");
        }
    }
}
