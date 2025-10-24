package com.aigreentick.services.template.dto;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateCreateResponseDto {
    private boolean success;
    private String templateId;
    private String status;
    private String category;
    private String errorMessage;
    private JsonNode errorPayload;
}