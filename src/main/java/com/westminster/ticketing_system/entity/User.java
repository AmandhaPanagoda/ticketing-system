package com.westminster.ticketing_system.entity;

import org.springframework.beans.BeanUtils;

import com.westminster.ticketing_system.dtos.UserDTO;
import com.westminster.ticketing_system.enums.UserRole;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

/**
 * Entity class representing a user in the system.
 * Contains user authentication and profile information.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    /** Unique identifier for the user */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Username */
    private String username;

    /** User's first name */
    private String firstName;

    /** User's last name */
    private String lastName;

    /** User's email address */
    private String email;

    /** User's encrypted password */
    private String password;

    /** User's phone number */
    private long phoneNumber;

    /** User's role in the system */
    private UserRole role;

    /**
     * Converts the entity to its DTO representation.
     * 
     * @return UserDTO containing the user's data
     */
    public UserDTO getDto() {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(this, userDTO);
        return userDTO;
    }

}
