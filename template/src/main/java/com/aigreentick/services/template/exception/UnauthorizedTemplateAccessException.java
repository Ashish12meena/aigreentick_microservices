package com.aigreentick.services.template.exception;

public class UnauthorizedTemplateAccessException extends RuntimeException {
    UnauthorizedTemplateAccessException(String message){
        super(message);
    }
}
