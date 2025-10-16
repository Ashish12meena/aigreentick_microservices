package com.aigreentick.services.template.dto.buildTemplateForRequest.utility;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UtilityFooterComponentDto extends UtilityComponentDto {
    private String text;
}
