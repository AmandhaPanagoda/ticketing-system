package com.westminster.ticketing_system.controllers;

import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.dtos.*;
import com.westminster.ticketing_system.services.authentication.AuthService;
import com.westminster.ticketing_system.services.jwt.UserDetailServiceImplementation;
import com.westminster.ticketing_system.util.JwtUtil;
import com.westminster.ticketing_system.services.systemLog.SystemLogService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller handling authentication operations including user signup and
 * token generation
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthenticationController {

        public static final String TOKEN_PREFIX = "Bearer ";
        public static final String HEADER_STRING = "Authorization";
        private static final String SOURCE = "AuthenticationController";
        private static final String ORIGINATOR = "SYSTEM";

        @Autowired
        private AuthService authService;

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private UserDetailServiceImplementation userDetailServiceImplementation;

        @Autowired
        private JwtUtil jwtUtil;

        @Autowired
        private SystemLogService logService;

        /**
         * Handles customer registration
         * 
         * @param signupDTO Registration details for the customer
         * @return ResponseEntity containing the created user details or error message
         */
        @PostMapping("/signup")
        public ResponseEntity<?> signupCustomer(@Valid @RequestBody SignupDTO signupDTO, BindingResult bindingResult) {
                logService.info(SOURCE, "Processing customer signup request for email: " + signupDTO.getEmail(),
                                ORIGINATOR, "signupCustomer");
                try {
                        // Check for validation errors
                        if (bindingResult.hasErrors()) {
                                String errors = bindingResult.getFieldErrors().stream()
                                                .map(error -> error.getDefaultMessage())
                                                .collect(Collectors.joining(", "));
                                logService.warn(SOURCE, "Signup validation failed: " + errors, ORIGINATOR,
                                                "signupCustomer");
                                return ResponseEntity.badRequest().body(errors);
                        }

                        if (authService.existsByEmail(signupDTO.getEmail().toLowerCase())) {
                                logService.warn(SOURCE, "Signup failed - Email already exists: " + signupDTO.getEmail(),
                                                ORIGINATOR, "signupCustomer");
                                return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already registered");
                        }

                        if (authService.existsByUsername(signupDTO.getUsername().toLowerCase())) {
                                logService.warn(SOURCE,
                                                "Signup failed - Username already exists: " + signupDTO.getUsername(),
                                                ORIGINATOR, "signupCustomer");
                                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
                        }

                        UserDTO createdUser = authService.signupCustomer(signupDTO);
                        logService.info(SOURCE, "Successfully created customer account for: " + signupDTO.getEmail(),
                                        ORIGINATOR, "signupCustomer");
                        return ResponseEntity.ok(createdUser);
                } catch (Exception e) {
                        logService.error(SOURCE, "Error during customer signup: " + e.getMessage(), ORIGINATOR,
                                        "signupCustomer");
                        return ResponseEntity.internalServerError()
                                        .body("An unexpected error occurred during registration");
                }
        }

        /**
         * Handles vendor registration
         * 
         * @param signupDTO Registration details for the vendor
         * @return ResponseEntity containing the created user details or error message
         */
        @PostMapping("/vendor/signup")
        public ResponseEntity<?> signupVendor(@Valid @RequestBody SignupDTO signupDTO, BindingResult bindingResult) {
                logService.info(SOURCE, "Processing vendor signup request for email: " + signupDTO.getEmail(),
                                ORIGINATOR, "signupVendor");
                try {
                        // Check for validation errors
                        if (bindingResult.hasErrors()) {
                                String errors = bindingResult.getFieldErrors().stream()
                                                .map(error -> error.getDefaultMessage())
                                                .collect(Collectors.joining(", "));
                                logService.warn(SOURCE, "Signup validation failed: " + errors, ORIGINATOR,
                                                "signupVendor");
                                return ResponseEntity.badRequest().body(errors);
                        }

                        if (authService.existsByEmail(signupDTO.getEmail().toLowerCase())) {
                                logService.warn(SOURCE, "Signup failed - Email already exists: " + signupDTO.getEmail(),
                                                ORIGINATOR, "signupVendor");
                                return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already registered");
                        }

                        if (authService.existsByUsername(signupDTO.getUsername().toLowerCase())) {
                                logService.warn(SOURCE,
                                                "Signup failed - Username already exists: " + signupDTO.getUsername(),
                                                ORIGINATOR, "signupVendor");
                                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
                        }

                        UserDTO createdUser = authService.signupVendor(signupDTO);
                        logService.info(SOURCE, "Successfully created vendor account for: " + signupDTO.getEmail(),
                                        ORIGINATOR, "signupVendor");
                        return ResponseEntity.ok(createdUser);
                } catch (Exception e) {
                        logService.error(SOURCE, "Error during vendor signup: " + e.getMessage(), ORIGINATOR,
                                        "signupVendor");
                        return ResponseEntity.internalServerError()
                                        .body("An unexpected error occurred during registration");
                }
        }

        /**
         * Handles admin registration (THIS IS FOR TEST PURPOSES ONLY)
         * 
         * @param signupDTO Registration details for the admin
         * @return ResponseEntity containing the created user details or error message
         */
        @PostMapping("/create-admin")
        public ResponseEntity<?> signupAdmin(@Valid @RequestBody SignupDTO signupDTO, BindingResult bindingResult) {
                logService.info(SOURCE, "Processing admin signup request for email: " + signupDTO.getEmail(),
                                ORIGINATOR, "signupAdmin");
                try {
                        // Check for validation errors
                        if (bindingResult.hasErrors()) {
                                String errors = bindingResult.getFieldErrors().stream()
                                                .map(error -> error.getDefaultMessage())
                                                .collect(Collectors.joining(", "));
                                logService.warn(SOURCE, "Signup validation failed: " + errors, ORIGINATOR,
                                                "signupAdmin");
                                return ResponseEntity.badRequest().body(errors);
                        }

                        if (authService.existsByEmail(signupDTO.getEmail().toLowerCase())) {
                                logService.warn(SOURCE, "Signup failed - Email already exists: " + signupDTO.getEmail(),
                                                ORIGINATOR, "signupAdmin");
                                return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already registered");
                        }

                        if (authService.existsByUsername(signupDTO.getUsername().toLowerCase())) {
                                logService.warn(SOURCE,
                                                "Signup failed - Username already exists: " + signupDTO.getUsername(),
                                                ORIGINATOR, "signupAdmin");
                                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
                        }

                        UserDTO createdUser = authService.signupVendor(signupDTO);
                        logService.info(SOURCE, "Successfully created admin account for: " + signupDTO.getEmail(),
                                        ORIGINATOR, "signupAdmin");
                        return ResponseEntity.ok(createdUser);
                } catch (Exception e) {
                        logService.error(SOURCE, "Error during admin signup: " + e.getMessage(), ORIGINATOR,
                                        "signupAdmin");
                        return ResponseEntity.internalServerError()
                                        .body("An unexpected error occurred during registration");
                }
        }

        /**
         * Authenticates user and generates JWT token
         * 
         * @param authenticationRequest Contains login credentials
         * @param response              HTTP response to add headers
         * @return ResponseEntity containing JWT token and user details
         */
        @PostMapping("/authenticate")
        public ResponseEntity<?> createAuthenticationToken(
                        @RequestBody AuthenticationRequest authenticationRequest,
                        HttpServletResponse response) {
                logService.info(SOURCE,
                                "Processing authentication request for user: " + authenticationRequest.getUsername(),
                                ORIGINATOR, "createAuthenticationToken");

                try {
                        // Authenticate user
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        authenticationRequest.getUsername(),
                                                        authenticationRequest.getPassword()));

                        // Generate token
                        final UserDetails userDetails = userDetailServiceImplementation
                                        .loadUserByUsername(authenticationRequest.getUsername());
                        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

                        // Check if user is deleted
                        UserDTO user = authService.getUserByEmail(authenticationRequest.getUsername());
                        if (user.getIsDeleted()) {
                                logService.warn(SOURCE,
                                                "Authentication failed for user " + authenticationRequest.getUsername()
                                                                + ": Account deactivated",
                                                ORIGINATOR, "createAuthenticationToken");
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .body("Account has been deactivated. Please contact support.");
                        }

                        // Prepare response
                        JSONObject responseBody = new JSONObject()
                                        .put("userId", user.getId())
                                        .put("role", user.getRole())
                                        .put("token", TOKEN_PREFIX + jwt);

                        // Set CORS headers
                        response.addHeader("Access-Control-Expose-Headers", HEADER_STRING);
                        response.addHeader("Access-Control-Allow-Headers",
                                        HEADER_STRING + ", X-PINGOTHER, Origin, Content-Type, Accept, X-Requested-With, X-Custom-header");
                        response.addHeader(HEADER_STRING, TOKEN_PREFIX + jwt);

                        logService.info(SOURCE,
                                        "Successfully authenticated user: " + authenticationRequest.getUsername(),
                                        ORIGINATOR, "createAuthenticationToken");
                        return ResponseEntity.ok(responseBody.toString());

                } catch (BadCredentialsException e) {
                        logService.warn(SOURCE, "Authentication failed for user " + authenticationRequest.getUsername()
                                        + ": Invalid credentials", ORIGINATOR, "createAuthenticationToken");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body("Invalid username or password");
                } catch (Exception e) {
                        logService.error(SOURCE, "Authentication error for user " + authenticationRequest.getUsername()
                                        + ": " + e.getMessage(), ORIGINATOR, "createAuthenticationToken");
                        return ResponseEntity.internalServerError()
                                        .body("An unexpected error occurred during authentication");
                }
        }
}
