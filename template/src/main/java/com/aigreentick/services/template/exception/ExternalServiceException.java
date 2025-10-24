package com.aigreentick.services.template.exception;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable ex) {
        super(message, ex);
    }
}
