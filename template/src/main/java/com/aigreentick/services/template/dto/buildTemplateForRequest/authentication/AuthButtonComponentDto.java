package com.aigreentick.services.template.dto.buildTemplateForRequest.authentication;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthButtonComponentDto extends AuthComponentDto {
    private List<AuthButtonDto> buttons; // ✅ match frontend format
}
