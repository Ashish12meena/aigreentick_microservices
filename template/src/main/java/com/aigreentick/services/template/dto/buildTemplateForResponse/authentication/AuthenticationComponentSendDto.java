package com.aigreentick.services.template.dto.buildTemplateForResponse.authentication;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationComponentSendDto  {
    private String type; // "body" or "button"
    private String sub_type; // only for button
    private String index;    // only for button
    private List<AuthenticationParameterDto> parameters;
}
