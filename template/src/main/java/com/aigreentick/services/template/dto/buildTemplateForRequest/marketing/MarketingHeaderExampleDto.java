package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing;

import java.util.List;

import lombok.Data;


@Data
public class MarketingHeaderExampleDto {
    List<String> headerText;   // for TEXT format
    List<String> headerHandle;
    
}
