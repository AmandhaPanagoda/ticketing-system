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
}