package com.westminster.ticketing_system.entity;

import org.springframework.beans.BeanUtils;

import com.westminster.ticketing_system.dtos.UserDTO;
import com.westminster.ticketing_system.enums.UserRole;

import jakarta.persistence.Column;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private long phoneNumber;
    private UserRole role;

    @Column(name = "deleted_ind", nullable = false)
    private Boolean isDeleted;

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
