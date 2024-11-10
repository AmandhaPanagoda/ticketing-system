package com.westminster.ticketing_system.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.dtos.UserDTO;
import com.westminster.ticketing_system.dtos.SystemConfigurationDTO;
import com.westminster.ticketing_system.dtos.TransactionLogDTO;
import com.westminster.ticketing_system.services.admin.AdminService;
import com.westminster.ticketing_system.services.transaction.TransactionLogService;
import com.westminster.ticketing_system.core.threads.ThreadManager;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TransactionLogService transactionLogService;

    @Autowired
    private ThreadManager threadManager;

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        return ResponseEntity.ok(adminService.getAllTickets());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{role}")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(adminService.getUsersByRole(role.toUpperCase()));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId) {
        boolean isDeleted = adminService.deleteUser(userId);
        if (isDeleted) {
            return ResponseEntity.ok("User deleted successfully");
        }
        return ResponseEntity.badRequest().body("Failed to delete user");
    }

    @PutMapping("/system-configuration")
    public ResponseEntity<?> updateSystemConfiguration(
            @RequestHeader("Userid") int userId,
            @RequestBody SystemConfigurationDTO configurationDTO) {
        boolean success = adminService.updateSystemConfiguration(userId, configurationDTO);
        if (success) {
            return ResponseEntity.ok()
                    .body(new HashMap<String, String>() {
                        {
                            put("message", "System configuration updated successfully");
                        }
                    });
        }
        return ResponseEntity.badRequest()
                .body(new HashMap<String, String>() {
                    {
                        put("message", "Failed to update system configuration");
                    }
                });
    }

    @GetMapping("/system-configuration")
    public ResponseEntity<SystemConfigurationDTO> getSystemConfiguration() {
        SystemConfigurationDTO config = adminService.getSystemConfiguration();
        return ResponseEntity.ok(config);
    }

    @PostMapping("/system")
    public ResponseEntity<?> controlSystem(@RequestBody boolean start) {
        try {
            if (start) {
                threadManager.startSystem();
                return ResponseEntity.ok("System started successfully");
            } else {
                threadManager.stopSystem();
                return ResponseEntity.ok("System stopped successfully");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to " + (start ? "start" : "stop") + " system: " + e.getMessage());
        }
    }

    @GetMapping("/system/status")
    public ResponseEntity<?> getSystemStatus() {
        return ResponseEntity.ok(Map.of("running", threadManager.isSystemRunning()));
    }

}