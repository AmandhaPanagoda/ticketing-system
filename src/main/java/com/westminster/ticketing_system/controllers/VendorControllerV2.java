package com.westminster.ticketing_system.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.core.threads.ThreadManager;
import com.westminster.ticketing_system.core.threads.VendorThread;
import com.westminster.ticketing_system.core.pool.TicketPool;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v2/vendor")
@Slf4j
public class VendorControllerV2 {

    @Autowired
    private ThreadManager threadManager;

    @Autowired
    private TicketPool ticketPool;

    // #region Version 2

    @PostMapping("/tickets/{userId}/{ticketCount}")
    public ResponseEntity<?> addTicketsV2(@PathVariable int userId, @PathVariable int ticketCount) {
        try {
            if (!threadManager.isSystemRunning()) {
                return ResponseEntity.badRequest().body("System is not running");
            }
            VendorThread vendorThread = new VendorThread(ticketPool, userId, ticketCount);
            threadManager.addVendorThread(vendorThread);
            vendorThread.start();
            return ResponseEntity.accepted()
                    .body(String.format("Processing request to add %d tickets from vendor %d", ticketCount, userId));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error processing vendor request: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to process ticket addition request");
        }
    }

    @GetMapping("/pool/status")
    public ResponseEntity<?> getPoolStatus() {
        return ResponseEntity.ok(Map.of(
                "currentTicketCount", ticketPool.getCurrentTicketCount(),
                "isFull", ticketPool.isPoolFull(),
                "isEmpty", ticketPool.isPoolEmpty()));
    }

    // #endregion

}
