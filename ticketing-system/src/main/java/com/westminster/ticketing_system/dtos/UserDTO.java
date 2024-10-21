package com.westminster.ticketing_system.dtos;

import com.westminster.ticketing_system.enums.UserRole;

import lombok.Data;

@Data
public class UserDTO {
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private long phoneNumber;
    private String password;
    private UserRole role;

}