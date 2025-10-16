package com.aigreentick.services.template.dto.buildTemplateForResponse.authentication;

import java.util.List;

import lombok.Data;

@Data
public class  AuthenticationTemplateDto {
    private String name;
    private AuthenticationLanguageDto language;
    private List<AuthenticationComponentSendDto> components;
}
