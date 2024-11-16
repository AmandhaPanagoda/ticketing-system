package com.westminster.ticketing_system.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.core.threads.CustomerThread;
import com.westminster.ticketing_system.core.threads.ThreadManager;
import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.dtos.TicketSummaryDTO;
import com.westminster.ticketing_system.core.pool.TicketPool;
import com.westminster.ticketing_system.services.Customer.CustomerService;

import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller handling customer operations for ticket purchases (V2)
 */
@RestController
@RequestMapping("/api/v2/customer")
@Slf4j
public class CustomerControllerV2 {

    @Autowired
    private ThreadManager threadManager;

    @Autowired
    private TicketPool ticketPool;

    @Autowired
    private CustomerService customerService;

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
        log.info("Received request to purchase {} tickets from customer {}", ticketCount, userId);

        try {
            if (!threadManager.isSystemRunning()) {
                log.warn("System not running - rejected request from customer {}", userId);
                return ResponseEntity.badRequest().body("System is currently not running");
            }

            CustomerThread customerThread = new CustomerThread(ticketPool, userId, ticketCount);
            threadManager.addCustomerThread(customerThread);
            customerThread.start();

            log.info("Successfully initiated ticket purchase process for customer {}", userId);
            return ResponseEntity.accepted()
                    .body(String.format("Processing request to purchase %d tickets", ticketCount));

        } catch (IllegalStateException e) {
            log.error("Invalid state for customer {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments from customer {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Invalid request parameters");
        } catch (Exception e) {
            log.error("Unexpected error processing customer {} request: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred while processing your request");
        }
    }

    /**
     * Retrieves current status of the ticket pool
     */
    @GetMapping("/pool/status")
    public ResponseEntity<?> getPoolStatus(@RequestHeader("Userid") int userId) {
        log.debug("Pool status requested by customer {}", userId);
        try {
            return ResponseEntity.ok(Map.of(
                    "currentTicketCount", ticketPool.getCurrentTicketCount(),
                    "isFull", ticketPool.isPoolFull(),
                    "isEmpty", ticketPool.isPoolEmpty(),
                    "isRunning", ticketPool.isRunning()));
        } catch (Exception e) {
            log.error("Error retrieving pool status for customer {}: {}", userId, e.getMessage(), e);
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
            log.info("Fetching tickets for customer with ID: {}", userId);
            List<TicketSummaryDTO> tickets = customerService.getCustomerTicketSummaries(userId);
            log.info("Successfully retrieved {} tickets for customer ID: {}", tickets.size(), userId);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("Error fetching tickets for customer ID: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred while processing your request");
        }
    }
}
