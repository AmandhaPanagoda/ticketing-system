package com.westminster.ticketing_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLogDTO { // TODO: SAVE IN DB
    private String type; // "PURCHASE" or "SALE"
    private LocalDateTime timestamp;
    private int userId;
    private String userRole;
    private int ticketId;
    private BigDecimal amount;
    private String status;
}