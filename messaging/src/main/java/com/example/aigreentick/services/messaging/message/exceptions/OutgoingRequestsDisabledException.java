package com.example.aigreentick.services.messaging.message.exceptions;

public class OutgoingRequestsDisabledException extends RuntimeException {

    private final int statusCode;

    public OutgoingRequestsDisabledException(String message) {
        super(message);
        this.statusCode = 503; // Service Unavailable
    }

    public OutgoingRequestsDisabledException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}