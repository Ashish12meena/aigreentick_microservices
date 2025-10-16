package com.aigreentick.services.template.dto.buildTemplateForRequest.authentication;

import com.aigreentick.services.template.dto.buildTemplateForRequest.BaseComponentDto;
import com.aigreentick.services.template.enums.ComponentType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = AuthBodyComponentDto.class, name = ComponentType.constants.BODY),
    @JsonSubTypes.Type(value = AuthFooterComponentDto.class, name = ComponentType.constants.FOOTER),
    @JsonSubTypes.Type(value = AuthButtonComponentDto.class, name = ComponentType.constants.BUTTONS)
})
@Data
public abstract class AuthComponentDto implements BaseComponentDto {
    private String type;
}
