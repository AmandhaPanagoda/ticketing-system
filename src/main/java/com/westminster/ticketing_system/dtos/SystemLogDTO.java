package com.westminster.ticketing_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogDTO {
    private String level; // INFO, ERROR, WARN, DEBUG
    private String source;
    private String message;
    private String userId;
    private String action;
    private LocalDateTime timestamp;

    public static SystemLogDTO createSystemLog(String level, String source, String message, String userId,
            String action, LocalDateTime timestamp) {
        return SystemLogDTO.builder()
                .level(level)
                .source(source)
                .message(message)
                .userId(userId)
                .action(action)
                .timestamp(timestamp)
                .build();
    }
}