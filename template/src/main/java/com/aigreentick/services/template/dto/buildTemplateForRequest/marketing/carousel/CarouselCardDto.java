package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel;

import java.util.List;

import lombok.Data;

@Data
public class CarouselCardDto {
    private List<CarouselCardComponentDto> components;
}
