package com.aigreentick.services.common.dto.response;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String code; // Unique error code
    private String message; // Human-readable message
    private String path; // Request URI
    private Map<String, Object> details; // Optional extra info
    private String traceId; // For distributed tracing
}
