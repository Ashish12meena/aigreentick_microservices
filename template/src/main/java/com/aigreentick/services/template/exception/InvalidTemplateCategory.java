package com.aigreentick.services.template.exception;

public class InvalidTemplateCategory extends RuntimeException {
    public InvalidTemplateCategory(String message) {
        super(message);
    }

    public InvalidTemplateCategory(String message, Throwable cause) {
        super(message, cause);
    }
}
