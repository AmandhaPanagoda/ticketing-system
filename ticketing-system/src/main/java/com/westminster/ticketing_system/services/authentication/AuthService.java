package com.westminster.ticketing_system.services.authentication;

import com.westminster.ticketing_system.dtos.SignupDTO;
import com.westminster.ticketing_system.dtos.UserDTO;

public interface AuthService {
    UserDTO signupCustomer(SignupDTO signupDTO);

    UserDTO signupVendor(SignupDTO signupDTO);

    Boolean existsByEmail(String email);
}
