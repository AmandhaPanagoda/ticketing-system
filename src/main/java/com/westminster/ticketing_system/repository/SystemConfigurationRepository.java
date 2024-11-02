package com.westminster.ticketing_system.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.westminster.ticketing_system.entity.SystemConfiguration;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Integer> {
    Optional<SystemConfiguration> findFirstByOrderByCreatedDateTimeDesc();
}
