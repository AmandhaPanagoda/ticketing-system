package com.westminster.ticketing_system.services.systemLog;

/**
 * Interface for system-wide logging operations.
 * Provides methods for different log levels and broadcasts logs via WebSocket.
 */
public interface SystemLogService {
    /**
     * Logs an informational message
     * 
     * @param source  The source/component generating the log
     * @param message The log message
     * @param userId  The ID of the user performing the action
     * @param action  The type of action being performed
     */
    void info(String source, String message, String userId, String action);

    /**
     * Logs an error message
     * 
     * @param source  The source/component generating the log
     * @param message The error message
     * @param userId  The ID of the user performing the action
     * @param action  The type of action being performed
     */
    void error(String source, String message, String userId, String action);

    /**
     * Logs a warning message
     * 
     * @param source  The source/component generating the log
     * @param message The warning message
     * @param userId  The ID of the user performing the action
     * @param action  The type of action being performed
     */
    void warn(String source, String message, String userId, String action);

    /**
     * Logs a debug message
     * 
     * @param source  The source/component generating the log
     * @param message The debug message
     * @param userId  The ID of the user performing the action
     * @param action  The type of action being performed
     */
    void debug(String source, String message, String userId, String action);
}
