package com.aigreentick.services.template.dto.buildTemplateForRequest.authentication;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthButtonDto {
    private String type; // should be "OTP" for authentication
    private String otpType; // "COPY_CODE" or "ONE_TAP"
    private String autofillText; // optional for autofill buttons
    private List<SupportedAppDto> supportedApps; // optional for ONE_TAP
    private String text;
}
