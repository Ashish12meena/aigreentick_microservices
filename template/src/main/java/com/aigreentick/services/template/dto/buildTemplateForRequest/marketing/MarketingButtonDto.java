package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing;

import java.util.List;


import lombok.Data;

@Data
// @ValidButtonType
public class MarketingButtonDto {
    private String type; // catalog , Quick Reply , Copy_code , url , flow
    private String text;
    private String url;         // if type is URL
    private String phoneNumber;  // if type is PHONE_NUMBER
    private String flowId;
    private String flowName;
    private String flowJson;
    private List<String> example;
}
