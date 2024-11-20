package com.westminster.ticketing_system.services.transaction;

import com.westminster.ticketing_system.dtos.TransactionLogDTO;
import com.westminster.ticketing_system.repository.UserRepository;
import com.westminster.ticketing_system.entity.User;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionLogService {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public TransactionLogService(
            SimpMessagingTemplate messagingTemplate,
            UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    public void addTransaction(TransactionLogDTO log) {
        // Fetch username
        User user = userRepository.findById(log.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TransactionLogDTO enrichedLog = TransactionLogDTO.builder()
                .type(log.getType())
                .timestamp(log.getTimestamp())
                .userId(log.getUserId())
                .username(user.getUsername())
                .userRole(log.getUserRole())
                .ticketId(log.getTicketId())
                .amount(log.getAmount())
                .status(log.getStatus())
                .build();

        // Send the enriched transaction log to subscribed clients
        messagingTemplate.convertAndSend("/topic/transactions", enrichedLog);
    }
}