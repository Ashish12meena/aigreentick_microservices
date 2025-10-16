package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing;

import com.aigreentick.services.template.dto.buildTemplateForRequest.BaseComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel.CarouselComponentDto;
import com.aigreentick.services.template.enums.ComponentType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = MarketingHeaderComponentDto.class, name = ComponentType.constants.HEADER),
    @JsonSubTypes.Type(value = MarketingBodyComponentDto.class, name = ComponentType.constants.BODY),
    @JsonSubTypes.Type(value = MarketingFooterComponentDto.class, name = ComponentType.constants.FOOTER),
    @JsonSubTypes.Type(value = MarketingButtonsComponentDto.class, name = ComponentType.constants.BUTTONS),
    @JsonSubTypes.Type(value = LimitedTimeOfferWrapperDto.class, name = ComponentType.constants.LIMITED_TIME_OFFER),
    @JsonSubTypes.Type(value = CarouselComponentDto.class, name = ComponentType.constants.CAROUSEL)
})

@Data
public abstract class MarketingComponentDto implements BaseComponentDto{
    private String type;
}
