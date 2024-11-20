package com.westminster.ticketing_system.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.dtos.UserDTO;
import com.westminster.ticketing_system.dtos.SystemConfigurationDTO;
import com.westminster.ticketing_system.services.admin.AdminService;
import com.westminster.ticketing_system.core.pool.TicketPool;
import com.westminster.ticketing_system.core.threads.ThreadManager;

import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller handling administrative operations for the ticketing system
 * Provides endpoints for user management, system configuration, and system
 * control
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ThreadManager threadManager;

    @Autowired
    private TicketPool ticketPool;

    /**
     * Retrieves all tickets in the system
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        log.debug("Retrieving all tickets");
        try {
            return ResponseEntity.ok(adminService.getAllTickets());
        } catch (Exception e) {
            log.error("Error retrieving tickets: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves all users in the system
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.debug("Retrieving all users");
        try {
            return ResponseEntity.ok(adminService.getAllUsers());
        } catch (Exception e) {
            log.error("Error retrieving users: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deletes a user from the system
     * 
     * @param userId The ID of the user to delete
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId) {
        log.info("Attempting to delete user with ID: {}", userId);
        try {
            boolean isDeleted = adminService.deleteUser(userId);
            if (isDeleted) {
                log.info("Successfully deleted user {}", userId);
                return ResponseEntity.ok("User deleted successfully");
            }
            log.warn("Failed to delete user {}", userId);
            return ResponseEntity.badRequest().body("Failed to delete user");
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred while deleting the user");
        }
    }

    /**
     * Updates system configuration settings
     * 
     * @param userId           ID of the admin making the change
     * @param configurationDTO New configuration settings
     */
    @PutMapping("/system-configuration")
    public ResponseEntity<?> updateSystemConfiguration(
            @RequestHeader("Userid") int userId,
            @RequestBody SystemConfigurationDTO configurationDTO) {
        log.info("Admin {} attempting to update system configuration", userId);
        try {
            boolean success = adminService.updateSystemConfiguration(userId, configurationDTO);
            if (success) {
                log.info("System configuration successfully updated by admin {}", userId);
                return ResponseEntity.ok(Map.of("message", "System configuration updated successfully"));
            }
            log.warn("Failed to update system configuration by admin {}", userId);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Failed to update system configuration"));
        } catch (Exception e) {
            log.error("Error updating system configuration by admin {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "An unexpected error occurred while updating configuration"));
        }
    }

    /**
     * Retrieves current system configuration
     */
    @GetMapping("/system-configuration")
    public ResponseEntity<SystemConfigurationDTO> getSystemConfiguration() {
        log.debug("Retrieving system configuration");
        try {
            SystemConfigurationDTO config = adminService.getSystemConfiguration();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error retrieving system configuration: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Controls system start/stop operations
     * 
     * @param start true to start the system, false to stop
     */
    @PostMapping("/system")
    public ResponseEntity<?> controlSystem(@RequestBody boolean start) {
        log.info("Attempting to {} system", start ? "start" : "stop");
        try {
            if (start) {
                threadManager.startSystem();
                log.info("System started successfully");
                return ResponseEntity.ok("System started successfully");
            } else {
                threadManager.stopSystem();
                log.info("System stopped successfully");
                return ResponseEntity.ok("System stopped successfully");
            }
        } catch (Exception e) {
            log.error("Failed to {} system: {}", start ? "start" : "stop", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to " + (start ? "start" : "stop") + " system: " + e.getMessage());
        }
    }

    /**
     * Retrieves current system running status
     */
    @GetMapping("/system/status")
    public ResponseEntity<?> getSystemStatus() {
        log.debug("Retrieving system status");
        try {
            return ResponseEntity.ok(Map.of("running", threadManager.isSystemRunning()));
        } catch (Exception e) {
            log.error("Error retrieving system status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to retrieve system status");
        }
    }

    /**
     * Retrieves current status of the ticket pool
     */
    @GetMapping("/pool/status")
    public ResponseEntity<?> getPoolStatus(@RequestHeader("Userid") int userId) {
        log.debug("Pool status requested by admin {}", userId);
        try {
            return ResponseEntity.ok(Map.of(
                    "currentTicketCount", ticketPool.getCurrentTicketCount(),
                    "isFull", ticketPool.isPoolFull(),
                    "isEmpty", ticketPool.isPoolEmpty(),
                    "isRunning", threadManager.isSystemRunning()));
        } catch (Exception e) {
            log.error("Error retrieving pool status for admin {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to retrieve pool status");
        }
    }
}