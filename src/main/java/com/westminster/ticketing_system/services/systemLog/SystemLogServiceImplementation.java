package com.westminster.ticketing_system.services.systemLog;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.westminster.ticketing_system.dtos.SystemLogDTO;
import com.westminster.ticketing_system.entity.SystemLog;
import com.westminster.ticketing_system.repository.SystemLogRepository;

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
    private final SystemLogRepository systemLogRepository;

    /**
     * Constructor for SystemLogServiceImplementation
     * 
     * @param messagingTemplate   WebSocket messaging template for broadcasting logs
     * @param systemLogRepository Repository for system logs
     */
    public SystemLogServiceImplementation(
            SimpMessagingTemplate messagingTemplate,
            SystemLogRepository systemLogRepository) {
        this.messagingTemplate = messagingTemplate;
        this.systemLogRepository = systemLogRepository;
    }

    @Override
    public void info(String source, String message, String userId, String action) {
        try {
            log.info("{} - {} - {}", source, action, message);
            saveAndBroadcastLog("INFO", source, message, userId, action);
        } catch (Exception e) {
            log.error("Failed to log INFO message: {}", e.getMessage());
        }
    }

    @Override
    public void error(String source, String message, String userId, String action) {
        try {
            log.error("{} - {} - {}", source, action, message);
            saveAndBroadcastLog("ERROR", source, message, userId, action);
        } catch (Exception e) {
            log.error("Failed to log ERROR message: {}", e.getMessage());
        }
    }

    @Override
    public void warn(String source, String message, String userId, String action) {
        try {
            log.warn("{} - {} - {}", source, action, message);
            saveAndBroadcastLog("WARN", source, message, userId, action);
        } catch (Exception e) {
            log.error("Failed to log WARN message: {}", e.getMessage());
        }
    }

    @Override
    public void debug(String source, String message, String userId, String action) {
        try {
            log.debug("{} - {} - {}", source, action, message);
            saveAndBroadcastLog("DEBUG", source, message, userId, action);
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
    private void saveAndBroadcastLog(String level, String source, String message,
            String userId, String action) {
        try {
            // Create and save entity
            SystemLog systemLog = new SystemLog(level, source, message, userId, action);
            systemLogRepository.save(systemLog);

            // Broadcast via WebSocket
            SystemLogDTO logDTO = SystemLogDTO.createSystemLog(
                    level,
                    source,
                    message,
                    userId,
                    action,
                    systemLog.getTimestamp());

            messagingTemplate.convertAndSend("/topic/logs", logDTO);
        } catch (Exception e) {
            log.error("Failed to save/broadcast log message: {}", e.getMessage());
        }
    }
}
