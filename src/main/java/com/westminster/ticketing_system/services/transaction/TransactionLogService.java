package com.westminster.ticketing_system.services.transaction;

import com.westminster.ticketing_system.dtos.TransactionLogDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class TransactionLogService {
    private final Queue<TransactionLogDTO> transactionQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, DeferredResult<List<TransactionLogDTO>>> waitingClients = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 30000; // 30 seconds
    // TODO SET A LIMIT FOR THE QUEUE SIZE

    public void addTransaction(TransactionLogDTO log) {
        transactionQueue.offer(log);
        notifyWaitingClients();
    }

    private void notifyWaitingClients() {
        List<TransactionLogDTO> currentLogs = new ArrayList<>(transactionQueue);
        waitingClients.forEach((clientId, result) -> {
            if (!result.isSetOrExpired()) {
                result.setResult(currentLogs);
            }
        });
        waitingClients.clear();
    }

    public DeferredResult<List<TransactionLogDTO>> waitForNewTransactions(String clientId) {
        DeferredResult<List<TransactionLogDTO>> result = new DeferredResult<>(TIMEOUT, new ArrayList<>());

        result.onCompletion(() -> waitingClients.remove(clientId));
        result.onTimeout(() -> waitingClients.remove(clientId));

        // If there are existing logs, return them immediately
        if (!transactionQueue.isEmpty()) {
            result.setResult(new ArrayList<>(transactionQueue));
            return result;
        }

        waitingClients.put(clientId, result);
        return result;
    }
}