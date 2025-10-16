package com.aigreentick.services.template.dto.buildTemplateForRequest.authentication;

import java.util.List;

import com.aigreentick.services.template.dto.buildTemplateForRequest.BaseTemplateRequestDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthTemplateRequestDto extends BaseTemplateRequestDto {
    private List<String> languages; // ✅ support multiple languages
    private List<AuthComponentDto> components;

     @Override
    public List<AuthComponentDto> getComponents() {
        return components;
    }
}
