package com.westminster.ticketing_system.dtos;

import lombok.Data;

/**
 * Data Transfer Object for user registration.
 * Contains necessary information for creating a new user account.
 */
@Data
public class SignupDTO {
    /** Desired username for the new account */
    private String username;

    /** User's first name */
    private String firstName;

    /** User's last name */
    private String lastName;

    /** Password for the new account */
    private String password;

    /** User's email address */
    private String email;

    /** User's phone number */
    private long phoneNumber;
}
