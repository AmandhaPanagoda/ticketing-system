package com.westminster.ticketing_system.dtos;

import lombok.Data;

@Data
public class SignupDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private long phoneNumber;
}
