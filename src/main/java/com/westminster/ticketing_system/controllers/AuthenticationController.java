package com.westminster.ticketing_system.controllers;

import java.io.IOException;

import org.json.JSONException;
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
import com.westminster.ticketing_system.entity.User;
import com.westminster.ticketing_system.repository.UserRepository;
import com.westminster.ticketing_system.services.authentication.AuthService;
import com.westminster.ticketing_system.services.jwt.UserDetailServiceImplementation;
import com.westminster.ticketing_system.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailServiceImplementation userDetailServiceImplementation;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

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

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest,
            HttpServletResponse response) throws IOException, JSONException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        final UserDetails userDetails = userDetailServiceImplementation
                .loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        User user = userRepository.findByEmail(authenticationRequest.getUsername());

        JSONObject responseBody = new JSONObject()
                .put("userId", user.getId())
                .put("role", user.getRole())
                .put("token", TOKEN_PREFIX + jwt);

        response.addHeader("Access-Control-Expose-Headers", HEADER_STRING);
        response.addHeader("Access-Control-Allow-Headers",
                HEADER_STRING + ", X-PINGOTHER, Origin, Content-Type, Accept, X-Requested-With, X-Custom-header");
        response.addHeader(HEADER_STRING, TOKEN_PREFIX + jwt);

        return ResponseEntity.ok(responseBody.toString());
    }

}
