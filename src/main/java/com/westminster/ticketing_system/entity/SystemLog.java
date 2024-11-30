package com.westminster.ticketing_system.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing system logs.
 * Stores log information for system events and operations.
 */
@Entity
@Table(name = "system_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String level; // INFO, ERROR, WARN, DEBUG

    @Column(nullable = false)
    private String source;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "user")
    private String userId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public SystemLog(String level, String source, String message, String userId, String action) {
        this.level = level;
        this.source = source;
        this.message = message;
        this.userId = userId;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }
}