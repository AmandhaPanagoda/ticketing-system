package com.westminster.ticketing_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.dtos.*;
import com.westminster.ticketing_system.services.authentication.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signupCustomer(@RequestBody SignupDTO signupDTO) {
        if (authService.existsByEmail(signupDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        UserDTO createdUser = authService.signupCustomer(signupDTO);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/vendor/signup")
    public ResponseEntity<?> signupVendor(@RequestBody SignupDTO signupDTO) {
        if (authService.existsByEmail(signupDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        UserDTO createdUser = authService.signupVendor(signupDTO);
        return ResponseEntity.ok(createdUser);
    }
}
