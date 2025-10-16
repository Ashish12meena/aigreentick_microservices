package com.aigreentick.services.storage.exception;

public class StorageLimitExceedException extends RuntimeException {
    public StorageLimitExceedException(String message) {
        super(message);
    }
    public StorageLimitExceedException(String message,Exception e) {
        super(message);
    }
    
}
