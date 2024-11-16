package com.westminster.ticketing_system.dtos;

import com.westminster.ticketing_system.enums.UserRole;

import lombok.Data;

/**
 * Data Transfer Object for User entity.
 * Used for transferring user data between layers without exposing entity
 * details.
 */
@Data
public class UserDTO {
    /** User's unique identifier */
    private int id;

    /** Username */
    private String username;

    /** User's first name */
    private String firstName;

    /** User's last name */
    private String lastName;

    /** User's email address */
    private String email;

    /** User's phone number */
    private long phoneNumber;

    /** User's password */
    private String password;

    /** User's role in the system */
    private UserRole role;
}
