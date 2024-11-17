package com.westminster.ticketing_system.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.westminster.ticketing_system.dtos.*;
import com.westminster.ticketing_system.services.authentication.AuthService;
import com.westminster.ticketing_system.services.jwt.UserDetailServiceImplementation;
import com.westminster.ticketing_system.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

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

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailServiceImplementation userDetailServiceImplementation;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Handles customer registration
     * 
     * @param signupDTO Registration details for the customer
     * @return ResponseEntity containing the created user details or error message
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signupCustomer(@RequestBody SignupDTO signupDTO) {
        log.info("Processing customer signup request for email: {}", signupDTO.getEmail());
        try {
            if (authService.existsByEmail(signupDTO.getEmail())) {
                log.warn("Signup failed - Email already exists: {}", signupDTO.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
            }

            if (authService.existsByUsername(signupDTO.getUsername())) {
                log.warn("Signup failed - Username already exists: {}", signupDTO.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
            }

            UserDTO createdUser = authService.signupCustomer(signupDTO);
            log.info("Successfully created customer account for: {}", signupDTO.getEmail());
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            log.error("Error during customer signup: {}", e.getMessage(), e);
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
    public ResponseEntity<?> signupVendor(@RequestBody SignupDTO signupDTO) {
        log.info("Processing vendor signup request for email: {}", signupDTO.getEmail());
        try {
            if (authService.existsByEmail(signupDTO.getEmail())) {
                log.warn("Signup failed - Email already exists: {}", signupDTO.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
            }

            if (authService.existsByUsername(signupDTO.getUsername())) {
                log.warn("Signup failed - Username already exists: {}", signupDTO.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
            }

            UserDTO createdUser = authService.signupVendor(signupDTO);
            log.info("Successfully created vendor account for: {}", signupDTO.getEmail());
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            log.error("Error during vendor signup: {}", e.getMessage(), e);
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
        log.info("Processing authentication request for user: {}", authenticationRequest.getUsername());

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()));

            // Get user details
            UserDTO user = authService.getUserByEmail(authenticationRequest.getUsername());

            // Generate token
            final UserDetails userDetails = userDetailServiceImplementation
                    .loadUserByUsername(authenticationRequest.getUsername());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());

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

            log.info("Successfully authenticated user: {}", authenticationRequest.getUsername());
            return ResponseEntity.ok(responseBody.toString());

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user {}: Invalid credentials",
                    authenticationRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        } catch (Exception e) {
            log.error("Authentication error for user {}: {}",
                    authenticationRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("An unexpected error occurred during authentication");
        }
    }
}
