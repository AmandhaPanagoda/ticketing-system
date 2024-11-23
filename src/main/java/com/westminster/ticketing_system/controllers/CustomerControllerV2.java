package com.westminster.ticketing_system.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.core.threads.CustomerThread;
import com.westminster.ticketing_system.core.threads.ThreadManager;
import com.westminster.ticketing_system.dtos.TicketSummaryDTO;
import com.westminster.ticketing_system.services.customer.CustomerService;
import com.westminster.ticketing_system.services.systemLog.SystemLogService;
import com.westminster.ticketing_system.core.pool.TicketPool;

import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller handling customer operations for ticket purchases (V2)
 */
@RestController
@RequestMapping("/api/v2/customer")
@PreAuthorize("hasRole('CUSTOMER')")
@Slf4j
public class CustomerControllerV2 {

    private static final String SOURCE = "CustomerControllerV2";
    private static final String ORIGINATOR = "SYSTEM";

    @Autowired
    private ThreadManager threadManager;

    @Autowired
    private TicketPool ticketPool;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SystemLogService logService;

    /**
     * Asynchronously processes ticket purchase requests from customers
     * 
     * @param userId      The customer's unique identifier
     * @param ticketCount Number of tickets to purchase
     * @return ResponseEntity with processing status
     */
    @PostMapping("/tickets/{ticketCount}")
    public ResponseEntity<?> removeTicketsV2(
            @RequestHeader("Userid") int userId,
            @PathVariable int ticketCount) {
        logService.info(SOURCE, "Received request to purchase " + ticketCount + " tickets from customer " + userId,
                ORIGINATOR, "removeTicketsV2");

        try {
            if (!threadManager.isSystemRunning()) {
                logService.warn(SOURCE, "System not running - rejected request from customer " + userId,
                        ORIGINATOR, "removeTicketsV2");
                return ResponseEntity.badRequest().body("System is currently not running");
            }

            CustomerThread customerThread = new CustomerThread(ticketPool, userId, ticketCount);
            threadManager.addCustomerThread(customerThread);
            customerThread.start();

            logService.info(SOURCE, "Successfully initiated ticket purchase process for customer " + userId,
                    ORIGINATOR, "removeTicketsV2");
            return ResponseEntity.accepted()
                    .body(String.format("Processing request to purchase %d tickets", ticketCount));

        } catch (IllegalStateException e) {
            logService.error(SOURCE, "Invalid state for customer " + userId + ": " + e.getMessage(),
                    ORIGINATOR, "removeTicketsV2");
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logService.error(SOURCE, "Invalid arguments from customer " + userId + ": " + e.getMessage(),
                    ORIGINATOR, "removeTicketsV2");
            return ResponseEntity.badRequest().body("Invalid request parameters");
        } catch (Exception e) {
            logService.error(SOURCE, "Unexpected error processing customer " + userId + " request: " + e.getMessage(),
                    ORIGINATOR, "removeTicketsV2");
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred while processing your request");
        }
    }

    /**
     * Retrieves current status of the ticket pool
     */
    @GetMapping("/pool/status")
    public ResponseEntity<?> getPoolStatus(@RequestHeader("Userid") int userId) {
        logService.debug(SOURCE, "Pool status requested by customer " + userId, ORIGINATOR, "getPoolStatus");
        try {
            return ResponseEntity.ok(Map.of(
                    "currentTicketCount", ticketPool.getCurrentTicketCount(),
                    "isFull", ticketPool.isPoolFull(),
                    "isEmpty", ticketPool.isPoolEmpty(),
                    "isRunning", ticketPool.isRunning()));
        } catch (Exception e) {
            logService.error(SOURCE, "Error retrieving pool status for customer " + userId + ": " + e.getMessage(),
                    ORIGINATOR, "getPoolStatus");
            return ResponseEntity.internalServerError()
                    .body("Failed to retrieve pool status");
        }
    }

    /**
     * Retrieves all tickets for a specific customer
     */
    @GetMapping("/tickets")
    public ResponseEntity<?> getCustomerTickets(@RequestHeader("Userid") int userId) {
        try {
            logService.info(SOURCE, "Fetching tickets for customer with ID: " + userId, ORIGINATOR,
                    "getCustomerTickets");
            List<TicketSummaryDTO> tickets = customerService.getCustomerTicketSummaries(userId);
            logService.info(SOURCE, "Successfully retrieved " + tickets.size() + " tickets for customer ID: " + userId,
                    ORIGINATOR, "getCustomerTickets");
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            logService.error(SOURCE, "Error fetching tickets for customer ID: " + userId + ": " + e.getMessage(),
                    ORIGINATOR, "getCustomerTickets");
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred while processing your request");
        }
    }
}
