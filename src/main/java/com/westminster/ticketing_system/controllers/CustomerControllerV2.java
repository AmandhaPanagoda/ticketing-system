package com.westminster.ticketing_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.core.threads.CustomerThread;
import com.westminster.ticketing_system.core.threads.ThreadManager;
import com.westminster.ticketing_system.core.pool.TicketPool;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v2/customer")
@Slf4j
public class CustomerControllerV2 {

    @Autowired
    private ThreadManager threadManager;

    @Autowired
    private TicketPool ticketPool;

    @PostMapping("/tickets/{userId}/{ticketCount}")
    public ResponseEntity<?> removeTicketsV2(@PathVariable int userId, @PathVariable int ticketCount) {
        try {
            CustomerThread customerThread = new CustomerThread(ticketPool, userId, ticketCount);
            threadManager.addCustomerThread(customerThread);
            customerThread.start();
            return ResponseEntity.accepted()
                    .body(String.format("Processing request to purchase %d tickets from customer %d", ticketCount,
                            userId));
        } catch (Exception e) {
            log.error("Error processing customer request: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to process ticket purchase request");
        }
    }

}
