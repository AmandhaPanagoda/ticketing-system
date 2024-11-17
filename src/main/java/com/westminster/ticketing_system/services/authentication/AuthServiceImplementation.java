package com.westminster.ticketing_system.services.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.westminster.ticketing_system.dtos.SignupDTO;
import com.westminster.ticketing_system.dtos.UserDTO;
import com.westminster.ticketing_system.entity.User;
import com.westminster.ticketing_system.enums.UserRole;
import com.westminster.ticketing_system.repository.UserRepository;

@Service
public class AuthServiceImplementation implements AuthService {
    @Autowired
    private UserRepository userRepository;

    public UserDTO signupCustomer(SignupDTO signupDTO) {
        User user = new User();
        user.setUsername(signupDTO.getUsername());
        user.setFirstName(signupDTO.getFirstName());
        user.setLastName(signupDTO.getLastName());
        user.setEmail(signupDTO.getEmail());
        user.setPhoneNumber(signupDTO.getPhoneNumber());
        user.setPassword(new BCryptPasswordEncoder().encode(signupDTO.getPassword()));
        user.setRole(UserRole.CUSTOMER);

        return userRepository.save(user).getDto();
    }

    public UserDTO signupVendor(SignupDTO signupDTO) {
        User user = new User();
        user.setUsername(signupDTO.getUsername());
        user.setFirstName(signupDTO.getFirstName());
        user.setLastName(signupDTO.getLastName());
        user.setEmail(signupDTO.getEmail());
        user.setPhoneNumber(signupDTO.getPhoneNumber());
        user.setPassword(new BCryptPasswordEncoder().encode(signupDTO.getPassword()));
        user.setRole(UserRole.VENDOR);

        return userRepository.save(user).getDto();
    }

    public Boolean existsByEmail(String email) {
        return userRepository.findByEmail(email) != null;
    }

    public Boolean existsByUsername(String username) {
        return userRepository.findByUsername(username) != null;
    }

    @Override
    public Boolean isDeleted(String email) {
        User user = userRepository.findByEmail(email);
        return user != null && user.getIsDeleted();
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user != null ? user.getDto() : null;
    }
}
