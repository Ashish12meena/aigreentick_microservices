package com.aigreentick.services.template.dto.buildTemplateForRequest.utility;

import com.aigreentick.services.template.dto.buildTemplateForRequest.BaseComponentDto;
import com.aigreentick.services.template.enums.ComponentType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = UtilityHeaderComponentDto.class, name = ComponentType.constants.HEADER),
    @JsonSubTypes.Type(value = UtilityBodyComponentDto.class, name = ComponentType.constants.BODY),
    @JsonSubTypes.Type(value = UtilityFooterComponentDto.class, name = ComponentType.constants.FOOTER),
    @JsonSubTypes.Type(value = UtilityButtonsComponentDto.class, name = ComponentType.constants.BUTTONS)
})

@Data
public abstract class UtilityComponentDto implements BaseComponentDto {
    private String type;
}
