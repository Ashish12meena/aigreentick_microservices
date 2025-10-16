package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel;

import java.util.List;

import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.MarketingComponentDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CarouselComponentDto extends MarketingComponentDto  {
    private List<CarouselCardDto> cards;
    private String type;
}