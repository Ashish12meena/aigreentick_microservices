package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CarouselHeaderComponentDto extends CarouselCardComponentDto {
    private String format; // IMAGE, VIDEO, DOCUMENT
    private CarouselHeaderExampleDto example;
}
