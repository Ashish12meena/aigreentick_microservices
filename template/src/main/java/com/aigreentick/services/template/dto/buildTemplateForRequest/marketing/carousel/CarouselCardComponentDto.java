package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel;

import com.aigreentick.services.template.enums.ComponentType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CarouselHeaderComponentDto.class, name = ComponentType.constants.HEADER),
    @JsonSubTypes.Type(value = CarouselButtonsComponentDto.class, name = ComponentType.constants.BUTTONS)
})

@Data
public abstract class CarouselCardComponentDto {
    private String type;
}
