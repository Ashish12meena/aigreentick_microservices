package com.aigreentick.services.template.dto.buildTemplateForRequest.authentication;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthFooterComponentDto extends AuthComponentDto {
    private Integer codeExpirationMinutes;
    private String text;
}
