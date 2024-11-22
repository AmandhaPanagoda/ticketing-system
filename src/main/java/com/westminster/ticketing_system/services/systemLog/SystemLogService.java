package com.westminster.ticketing_system.services.systemLog;

public interface SystemLogService {
    void info(String source, String message, String userId, String action);

    void error(String source, String message, String userId, String action);

    void warn(String source, String message, String userId, String action);

    void debug(String source, String message, String userId, String action);
}
