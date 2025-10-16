package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MarketingButtonsComponentDto extends MarketingComponentDto {
    private List<MarketingButtonDto> buttons;
}
