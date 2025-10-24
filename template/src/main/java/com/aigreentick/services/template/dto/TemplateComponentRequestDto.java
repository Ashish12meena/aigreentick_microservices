package com.aigreentick.services.template.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateComponentRequestDto {
    private String type; // HEADER, BODY, FOOTER, BUTTONS
    private String format; // TEXT, IMAGE, VIDEO, DOCUMENT
    private String text; // For TEXT-based components
    private List<String> mediaUrls; // For IMAGE, VIDEO, DOCUMENT (only used in HEADER)
    private List<String> textExamples; // example for text variable
    private List<TemplateComponentButtonRequestDto> buttons; // Only for BUTTON component
}
