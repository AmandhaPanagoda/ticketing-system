package com.westminster.ticketing_system.services.transaction;

import com.westminster.ticketing_system.dtos.TransactionLogDTO;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionLogService {
    private final SimpMessagingTemplate messagingTemplate;

    public TransactionLogService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void addTransaction(TransactionLogDTO log) {
        // Send the transaction log directly to subscribed clients
        messagingTemplate.convertAndSend("/topic/transactions", log);
    }
}