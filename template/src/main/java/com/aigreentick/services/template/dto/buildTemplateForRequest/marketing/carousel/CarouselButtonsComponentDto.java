package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CarouselButtonsComponentDto extends CarouselCardComponentDto {
    private List<CarouselButtonDto> buttons;
}
