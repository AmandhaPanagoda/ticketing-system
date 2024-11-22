package com.westminster.ticketing_system.services.systemLog;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.westminster.ticketing_system.dtos.SystemLogDTO;

import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of SystemLogService that handles both console logging
 * and WebSocket broadcasting of system logs.
 */
@Service
@Slf4j
public class SystemLogServiceImplementation implements SystemLogService {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructor for SystemLogServiceImplementation
     * 
     * @param messagingTemplate WebSocket messaging template for broadcasting logs
     */
    public SystemLogServiceImplementation(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void info(String source, String message, String userId, String action) {
        try {
            log.info("{} - {} - {}", source, action, message);
            broadcastLog("INFO", source, message, userId, action);
        } catch (Exception e) {
            log.error("Failed to log INFO message: {}", e.getMessage());
        }
    }

    @Override
    public void error(String source, String message, String userId, String action) {
        try {
            log.error("{} - {} - {}", source, action, message);
            broadcastLog("ERROR", source, message, userId, action);
        } catch (Exception e) {
            log.error("Failed to log ERROR message: {}", e.getMessage());
        }
    }

    @Override
    public void warn(String source, String message, String userId, String action) {
        try {
            log.warn("{} - {} - {}", source, action, message);
            broadcastLog("WARN", source, message, userId, action);
        } catch (Exception e) {
            log.error("Failed to log WARN message: {}", e.getMessage());
        }
    }

    @Override
    public void debug(String source, String message, String userId, String action) {
        try {
            log.debug("{} - {} - {}", source, action, message);
            broadcastLog("DEBUG", source, message, userId, action);
        } catch (Exception e) {
            log.error("Failed to log DEBUG message: {}", e.getMessage());
        }
    }

    /**
     * Broadcasts a log entry to WebSocket subscribers
     * 
     * @param level   Log level (INFO, ERROR, WARN, DEBUG)
     * @param source  Source of the log
     * @param message Log message
     * @param userId  User ID associated with the log
     * @param action  Action being performed
     */
    private void broadcastLog(String level, String source, String message,
            String userId, String action) {
        try {
            SystemLogDTO logEntry = SystemLogDTO.builder()
                    .level(level)
                    .source(source)
                    .message(message)
                    .userId(userId)
                    .action(action)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/logs", logEntry);
        } catch (Exception e) {
            log.error("Failed to broadcast log message: {}", e.getMessage());
        }
    }
}
