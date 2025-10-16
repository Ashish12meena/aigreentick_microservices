package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MarketingHeaderComponentDto extends MarketingComponentDto {
    private String format; // TEXT, IMAGE, VIDEO, DOCUMENT
    private String text;
    private MarketingHeaderExampleDto example;
}
