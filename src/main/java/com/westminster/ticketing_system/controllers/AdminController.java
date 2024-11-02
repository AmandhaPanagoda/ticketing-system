package com.westminster.ticketing_system.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.dtos.UserDTO;
import com.westminster.ticketing_system.dtos.SystemConfigurationDTO;
import com.westminster.ticketing_system.services.admin.AdminService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

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
            @RequestHeader("userId") int userId,
            @RequestBody SystemConfigurationDTO configurationDTO) {
        boolean success = adminService.updateSystemConfiguration(userId, configurationDTO);
        if (success) {
            return ResponseEntity.ok("System configuration updated successfully");
        }
        return ResponseEntity.badRequest().body("Failed to update system configuration");
    }

    @GetMapping("/system-configuration")
    public ResponseEntity<SystemConfigurationDTO> getSystemConfiguration() {
        SystemConfigurationDTO config = adminService.getSystemConfiguration();
        return ResponseEntity.ok(config);
    }
}