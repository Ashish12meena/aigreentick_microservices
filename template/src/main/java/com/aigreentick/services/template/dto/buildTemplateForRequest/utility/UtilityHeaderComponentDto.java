package com.aigreentick.services.template.dto.buildTemplateForRequest.utility;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UtilityHeaderComponentDto extends UtilityComponentDto{
     private String format; // TEXT, IMAGE, VIDEO, DOCUMENT
    private String text;
    private UtilityHeaderExampleDto example;
}
