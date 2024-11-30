package com.westminster.ticketing_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.westminster.ticketing_system.entity.SystemLog;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findBySourceOrderByTimestampDesc(String source);

    List<SystemLog> findByLevelOrderByTimestampDesc(String level);
}