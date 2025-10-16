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
public class TemplateComponentButtonRequestDto {

     private String type; // QUICK_REPLY, PHONE_NUMBER, URL
    private String text;

    // For PHONE_NUMBER
    private String phoneNumber;
    
    // For URL
    private String url;
    private List<String> urlExamples;
}
