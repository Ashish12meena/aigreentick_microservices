package com.aigreentick.services.template.exception;

public class TemplateAlreadyExistsException extends RuntimeException {
    public TemplateAlreadyExistsException(String message) {
        super(message);
    }
}
