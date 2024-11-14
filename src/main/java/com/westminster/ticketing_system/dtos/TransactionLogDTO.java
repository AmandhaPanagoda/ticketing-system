package com.westminster.ticketing_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.westminster.ticketing_system.enums.Transaction;
import com.westminster.ticketing_system.enums.UserRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLogDTO { // TODO: SAVE IN DB
    private Transaction type;
    private LocalDateTime timestamp;
    private int userId;
    private UserRole userRole;
    private int ticketId;
    private BigDecimal amount;
    private String status;

    /**
     * Creates a successful transaction log entry with current timestamp.
     * 
     * @param type     The type of transaction (PURCHASE, SALE, etc.)
     * @param userId   The ID of the user performing the transaction
     * @param userRole The role of the user (CUSTOMER, VENDOR)
     * @param ticketId The ID of the ticket involved
     * @param amount   The transaction amount
     * @return TransactionLogDTO instance
     * @throws IllegalArgumentException if required parameters are invalid
     */
    public static TransactionLogDTO createSuccessfulTransaction(
            Transaction type, int userId, UserRole userRole,
            int ticketId, BigDecimal amount) {
        return TransactionLogDTO.builder()
                .type(type)
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .userRole(userRole)
                .ticketId(ticketId)
                .amount(amount)
                .status("SUCCESS")
                .build();
    }
}