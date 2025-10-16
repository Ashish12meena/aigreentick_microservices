package com.aigreentick.services.template.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.aigreentick.services.common.dto.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class TemplateGlobalException {

    @ExceptionHandler(TemplateAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleTemplateExists(
            TemplateAlreadyExistsException ex, HttpServletRequest request) {

        log.warn("Template conflict: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(UnauthorizedTemplateAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedTemplateAccessException(
            UnauthorizedTemplateAccessException ex, HttpServletRequest request) {

        log.warn("Template forbidden: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTemplateNotExists(
            TemplateNotFoundException ex, HttpServletRequest request) {

        log.warn("Template not found: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Field-level errors
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );

        // Class-level errors (custom validator)
        ex.getBindingResult().getGlobalErrors().forEach(error -> 
            errors.put(error.getObjectName(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }

    // Helper method for building consistent ErrorResponse
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception ex, HttpStatus status, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
