package com.westminster.ticketing_system.services.authentication;

import org.springframework.beans.factory.annotation.Autowired;
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
        user.setPassword(signupDTO.getPassword());
        user.setRole(UserRole.CUSTOMER);

        return userRepository.save(user).getDto();
    }
}
