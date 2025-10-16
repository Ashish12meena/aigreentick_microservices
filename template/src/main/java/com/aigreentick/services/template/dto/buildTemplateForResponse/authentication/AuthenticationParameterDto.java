package com.aigreentick.services.template.dto.buildTemplateForResponse.authentication;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthenticationParameterDto {
    private String type = "text";
    private String text;

    public AuthenticationParameterDto(String text) {
        this.text = text;
    }
}
