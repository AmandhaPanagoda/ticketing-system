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
import com.westminster.ticketing_system.services.systemLog.SystemLogService;

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

    private static final String SOURCE = "AdminController";
    private static final String ORIGINATOR = "SYSTEM";

    @Autowired
    private AdminService adminService;

    @Autowired
    private ThreadManager threadManager;

    @Autowired
    private TicketPool ticketPool;

    @Autowired
    private SystemLogService logService;

    /**
     * Retrieves all tickets in the system
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        logService.info(SOURCE, "Retrieving all tickets", ORIGINATOR, "getAllTickets");
        try {
            return ResponseEntity.ok(adminService.getAllTickets());
        } catch (Exception e) {
            logService.error(SOURCE, "Error retrieving tickets: " + e.getMessage(), ORIGINATOR, "getAllTickets");
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves all users in the system
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logService.info(SOURCE, "Retrieving all users", ORIGINATOR, "getAllUsers");
        try {
            return ResponseEntity.ok(adminService.getAllUsers());
        } catch (Exception e) {
            logService.error(SOURCE, "Error retrieving users: " + e.getMessage(), ORIGINATOR, "getAllUsers");
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
        logService.info(SOURCE, "Attempting to delete user with ID: " + userId, ORIGINATOR, "deleteUser");
        try {
            boolean isDeleted = adminService.deleteUser(userId);
            if (isDeleted) {
                logService.info(SOURCE, "Successfully deleted user " + userId, ORIGINATOR, "deleteUser");
                return ResponseEntity.ok("User deleted successfully");
            }
            logService.warn(SOURCE, "Failed to delete user " + userId, ORIGINATOR, "deleteUser");
            return ResponseEntity.badRequest().body("Failed to delete user");
        } catch (Exception e) {
            logService.error(SOURCE, "Error deleting user " + userId + ": " + e.getMessage(), ORIGINATOR, "deleteUser");
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred while deleting the user");
        }
    }

    /**
     * Activates a user
     * 
     * @param userId The ID of the user to activate
     */
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable int userId) {
        logService.info(SOURCE, "Attempting to activate user with ID: " + userId, ORIGINATOR, "activateUser");
        try {
            boolean isActivated = adminService.activateUser(userId);
            if (isActivated) {
                logService.info(SOURCE, "Successfully activated user " + userId, ORIGINATOR, "activateUser");
                return ResponseEntity.ok("User activated successfully");
            }
            logService.warn(SOURCE, "Failed to activate user " + userId, ORIGINATOR, "activateUser");
            return ResponseEntity.badRequest().body("Failed to activate user");
        } catch (Exception e) {
            logService.error(SOURCE, "Error activating user " + userId + ": " + e.getMessage(), ORIGINATOR,
                    "activateUser");
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred while activating the user");
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
        logService.info(SOURCE, "Admin " + userId + " attempting to update system configuration", ORIGINATOR,
                "updateSystemConfiguration");
        try {
            boolean success = adminService.updateSystemConfiguration(userId, configurationDTO);
            if (success) {
                logService.info(SOURCE, "System configuration successfully updated by admin " + userId, ORIGINATOR,
                        "updateSystemConfiguration");
                return ResponseEntity.ok(Map.of("message", "System configuration updated successfully"));
            }
            logService.warn(SOURCE, "Failed to update system configuration by admin " + userId, ORIGINATOR,
                    "updateSystemConfiguration");
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Failed to update system configuration"));
        } catch (Exception e) {
            logService.error(SOURCE, "Error updating system configuration by admin " + userId + ": " + e.getMessage(),
                    ORIGINATOR, "updateSystemConfiguration");
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "An unexpected error occurred while updating configuration"));
        }
    }

    /**
     * Retrieves current system configuration
     */
    @GetMapping("/system-configuration")
    public ResponseEntity<SystemConfigurationDTO> getSystemConfiguration() {
        logService.info(SOURCE, "Retrieving system configuration", ORIGINATOR, "getSystemConfiguration");
        try {
            SystemConfigurationDTO config = adminService.getSystemConfiguration();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logService.error(SOURCE, "Error retrieving system configuration: " + e.getMessage(), ORIGINATOR,
                    "getSystemConfiguration");
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
        logService.info(SOURCE, "Attempting to " + (start ? "start" : "stop") + " system", ORIGINATOR,
                "controlSystem");
        try {
            if (start) {
                threadManager.startSystem();
                logService.info(SOURCE, "System started successfully", ORIGINATOR, "controlSystem");
                return ResponseEntity.ok("System started successfully");
            } else {
                threadManager.stopSystem();
                logService.info(SOURCE, "System stopped successfully", ORIGINATOR, "controlSystem");
                return ResponseEntity.ok("System stopped successfully");
            }
        } catch (Exception e) {
            logService.error(SOURCE, "Failed to " + (start ? "start" : "stop") + " system: " + e.getMessage(),
                    ORIGINATOR, "controlSystem");
            return ResponseEntity.internalServerError()
                    .body("Failed to " + (start ? "start" : "stop") + " system: " + e.getMessage());
        }
    }

    /**
     * Retrieves current system running status
     */
    @GetMapping("/system/status")
    public ResponseEntity<?> getSystemStatus() {
        logService.info(SOURCE, "Retrieving system status", ORIGINATOR, "getSystemStatus");
        try {
            return ResponseEntity.ok(Map.of("running", threadManager.isSystemRunning()));
        } catch (Exception e) {
            logService.error(SOURCE, "Error retrieving system status: " + e.getMessage(), ORIGINATOR,
                    "getSystemStatus");
            return ResponseEntity.internalServerError()
                    .body("Failed to retrieve system status");
        }
    }

    /**
     * Retrieves current status of the ticket pool
     */
    @GetMapping("/pool/status")
    public ResponseEntity<?> getPoolStatus(@RequestHeader("Userid") int userId) {
        logService.info(SOURCE, "Pool status requested by admin " + userId, ORIGINATOR, "getPoolStatus");

        try {
            return ResponseEntity.ok(Map.of(
                    "currentTicketCount", ticketPool.getCurrentTicketCount(),
                    "isFull", ticketPool.isPoolFull(),
                    "isEmpty", ticketPool.isPoolEmpty(),
                    "isRunning", threadManager.isSystemRunning()));
        } catch (Exception e) {
            logService.error(SOURCE, "Error retrieving pool status for admin " + userId + ": " + e.getMessage(),
                    ORIGINATOR, "getPoolStatus");
            return ResponseEntity.internalServerError()
                    .body("Failed to retrieve pool status");
        }
    }
}