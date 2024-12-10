package com.westminster.ticketing_system.services.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.westminster.ticketing_system.dtos.SignupDTO;
import com.westminster.ticketing_system.dtos.UserDTO;
import com.westminster.ticketing_system.entity.User;
import com.westminster.ticketing_system.enums.UserRole;
import com.westminster.ticketing_system.repository.UserRepository;

@Service
@Slf4j
public class AuthServiceImplementation implements AuthService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDTO signupCustomer(SignupDTO signupDTO) {
        try {
            log.info("Creating new customer account for username: {}", signupDTO.getUsername());
            User user = new User();
            user.setUsername(signupDTO.getUsername().toLowerCase());
            user.setFirstName(signupDTO.getFirstName());
            user.setLastName(signupDTO.getLastName());
            user.setEmail(signupDTO.getEmail().toLowerCase());
            user.setPhoneNumber(signupDTO.getPhoneNumber());
            user.setPassword(new BCryptPasswordEncoder().encode(signupDTO.getPassword()));
            user.setRole(UserRole.CUSTOMER);
            user.setIsDeleted(false);

            UserDTO savedUser = userRepository.save(user).getDto();
            log.info("Successfully created customer account for: {}", signupDTO.getUsername());
            return savedUser;
        } catch (Exception e) {
            log.error("Error creating customer account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create customer account", e);
        }
    }

    @Override
    public UserDTO signupVendor(SignupDTO signupDTO) {
        try {
            log.info("Creating new vendor account for username: {}", signupDTO.getUsername());
            User user = new User();
            user.setUsername(signupDTO.getUsername().toLowerCase());
            user.setFirstName(signupDTO.getFirstName());
            user.setLastName(signupDTO.getLastName());
            user.setEmail(signupDTO.getEmail().toLowerCase());
            user.setPhoneNumber(signupDTO.getPhoneNumber());
            user.setPassword(new BCryptPasswordEncoder().encode(signupDTO.getPassword()));
            user.setRole(UserRole.VENDOR);
            user.setIsDeleted(false);

            UserDTO savedUser = userRepository.save(user).getDto();
            log.info("Successfully created vendor account for: {}", signupDTO.getUsername());
            return savedUser;
        } catch (Exception e) {
            log.error("Error creating vendor account: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create vendor account", e);
        }
    }

    @Override
    public Boolean existsByEmail(String email) {
        try {
            log.debug("Checking if email exists: {}", email);
            return userRepository.findByEmail(email) != null;
        } catch (Exception e) {
            log.error("Error checking email existence: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check email existence", e);
        }
    }

    @Override
    public Boolean existsByUsername(String username) {
        try {
            log.debug("Checking if username exists: {}", username);
            return userRepository.findByUsername(username) != null;
        } catch (Exception e) {
            log.error("Error checking username existence: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check username existence", e);
        }
    }

    @Override
    public Boolean isDeleted(String email) {
        try {
            log.debug("Checking if user is deleted for email: {}", email);
            User user = userRepository.findByEmail(email);
            return user != null && user.getIsDeleted();
        } catch (Exception e) {
            log.error("Error checking user deletion status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check user deletion status", e);
        }
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        try {
            log.debug("Fetching user by email: {}", email);
            User user = userRepository.findByEmail(email);
            if (user == null) {
                log.warn("No user found for email: {}", email);
                return null;
            }
            return user.getDto();
        } catch (Exception e) {
            log.error("Error fetching user by email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user by email", e);
        }
    }

    @Override
    public UserDTO getUserById(int userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? user.getDto() : null;
    }
}
