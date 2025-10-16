package com.aigreentick.services.template.dto.buildTemplateForRequest.utility;

import java.util.List;

import lombok.Data;

@Data
public class UtilityButtonDto {
    private String type; // QUICK_REPLY, URL, PHONE_NUMBER
    private String text;
    private String url;
    private String phoneNumber;
    private String flowId;
    private String flowName;
    private String flowJson;
    private List<String> example;
}
