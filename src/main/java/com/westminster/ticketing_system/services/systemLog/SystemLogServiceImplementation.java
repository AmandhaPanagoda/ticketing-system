package com.westminster.ticketing_system.services.systemLog;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.westminster.ticketing_system.dtos.SystemLogDTO;

import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SystemLogServiceImplementation implements SystemLogService {
    private final SimpMessagingTemplate messagingTemplate;

    public SystemLogServiceImplementation(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void info(String source, String message, String userId, String action) {
        log.info("{} - {} - {}", source, action, message);
        broadcastLog("INFO", source, message, userId, action);
    }

    public void error(String source, String message, String userId, String action) {
        log.error("{} - {} - {}", source, action, message);
        broadcastLog("ERROR", source, message, userId, action);
    }

    public void warn(String source, String message, String userId, String action) {
        log.warn("{} - {} - {}", source, action, message);
        broadcastLog("WARN", source, message, userId, action);
    }

    public void debug(String source, String message, String userId, String action) {
        log.debug("{} - {} - {}", source, action, message);
        broadcastLog("DEBUG", source, message, userId, action);
    }

    private void broadcastLog(String level, String source, String message,
            String userId, String action) {
        SystemLogDTO logEntry = SystemLogDTO.builder()
                .level(level)
                .source(source)
                .message(message)
                .userId(userId)
                .action(action)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/logs", logEntry);
    }
}
