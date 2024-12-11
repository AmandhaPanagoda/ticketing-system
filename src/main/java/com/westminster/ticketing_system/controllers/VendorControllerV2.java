package com.westminster.ticketing_system.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.core.threads.ThreadManager;
import com.westminster.ticketing_system.core.threads.VendorThread;
import com.westminster.ticketing_system.core.pool.TicketPool;
import com.westminster.ticketing_system.services.vendor.VendorService;
import com.westminster.ticketing_system.services.systemLog.SystemLogService;
import com.westminster.ticketing_system.dtos.TicketSummaryDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller handling vendor operations for ticket management (V2)
 */
@RestController
@RequestMapping("/api/v2/vendor")
@PreAuthorize("hasRole('VENDOR')")
@Slf4j
public class VendorControllerV2 {

    @Autowired
    private ThreadManager threadManager;

    @Autowired
    private TicketPool ticketPool;

    @Autowired
    private VendorService vendorService;

    @Autowired
    private SystemLogService logService;

    private static final String SOURCE = "VendorControllerV2";
    private static final String ORIGINATOR = "SYSTEM";

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
        logService.info(SOURCE, "Received request to add " + ticketCount + " tickets from vendor " + userId,
                ORIGINATOR, "addTicketsV2");

        try {
            if (!threadManager.isSystemRunning()) {
                logService.warn(SOURCE, "System not running - rejected request from vendor " + userId,
                        ORIGINATOR, "addTicketsV2");
                return ResponseEntity.badRequest().body("System is currently not running");
            }

            if (ticketCount < 0) {
                logService.warn(SOURCE, "Cannot add tickets less than 0. Request from " + userId,
                        ORIGINATOR, "addTicketsV2");
                return ResponseEntity.badRequest().body("Please enter a valid amount of tickets.");
            }

            VendorThread vendorThread = new VendorThread(ticketPool, userId, ticketCount);
            threadManager.addVendorThread(vendorThread);
            vendorThread.start();

            logService.info(SOURCE, "Successfully initiated ticket addition process for vendor " + userId,
                    ORIGINATOR, "addTicketsV2");
            return ResponseEntity.accepted()
                    .body(String.format("Processing request to add %d tickets", ticketCount));

        } catch (IllegalStateException e) {
            logService.error(SOURCE, "Invalid state for vendor " + userId + ": " + e.getMessage(),
                    ORIGINATOR, "addTicketsV2");
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logService.error(SOURCE, "Invalid arguments from vendor " + userId + ": " + e.getMessage(),
                    ORIGINATOR, "addTicketsV2");
            return ResponseEntity.badRequest().body("Invalid request parameters");
        } catch (Exception e) {
            logService.error(SOURCE, "Unexpected error processing vendor " + userId + " request: " + e.getMessage(),
                    ORIGINATOR, "addTicketsV2");
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred while processing your request");
        }
    }

    /**
     * Retrieves current status of the ticket pool
     */
    @GetMapping("/pool/status")
    public ResponseEntity<?> getPoolStatus(@RequestHeader("Userid") int userId) {
        logService.debug(SOURCE, "Pool status requested by vendor " + userId, ORIGINATOR, "getPoolStatus");
        try {
            return ResponseEntity.ok(Map.of(
                    "currentTicketCount", ticketPool.getCurrentTicketCount(),
                    "isFull", ticketPool.isPoolFull(),
                    "isEmpty", ticketPool.isPoolEmpty(),
                    "isRunning", threadManager.isSystemRunning()));
        } catch (Exception e) {
            logService.error(SOURCE, "Error retrieving pool status for vendor " + userId + ": " + e.getMessage(),
                    ORIGINATOR, "getPoolStatus");
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
            logService.info(SOURCE, "Fetching ticket summaries for vendor with ID: " + userId, ORIGINATOR,
                    "getVendorTickets");
            List<TicketSummaryDTO> tickets = vendorService.getVendorTicketSummaries(userId);
            logService.info(SOURCE, "Successfully retrieved " + tickets.size() + " ticket summaries for vendor ID: "
                    + userId, ORIGINATOR, "getVendorTickets");
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            logService.error(SOURCE, "Error fetching ticket summaries for vendor ID: " + userId + ": " + e.getMessage(),
                    ORIGINATOR, "getVendorTickets");
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred while processing your request");
        }
    }
}
