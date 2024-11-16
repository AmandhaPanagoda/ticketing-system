package com.westminster.ticketing_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.westminster.ticketing_system.entity.User;
import com.westminster.ticketing_system.enums.UserRole;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);

    List<User> findByRole(UserRole role);

    User findByUsername(String username);
}
