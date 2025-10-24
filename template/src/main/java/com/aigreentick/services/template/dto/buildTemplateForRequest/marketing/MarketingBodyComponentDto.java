package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MarketingBodyComponentDto extends MarketingComponentDto {
    private String text;
    private MarketingBodyExampleDto example;
}
