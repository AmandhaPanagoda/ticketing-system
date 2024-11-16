package com.westminster.ticketing_system.dtos;

import lombok.Data;

/**
 * Data Transfer Object for user registration.
 * Contains necessary information for creating a new user account.
 */
@Data
public class SignupDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private long phoneNumber;
}
