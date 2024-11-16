package com.westminster.ticketing_system.dtos;

import lombok.Data;

/**
 * Data Transfer Object for authentication requests.
 * Contains credentials needed for user authentication.
 */
@Data
public class AuthenticationRequest {
    /** Username for authentication, actually the email */
    private String username;

    /** Password for authentication */
    private String password;
}
