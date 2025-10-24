package com.example.aigreentick.services.messaging.message.model.content.interactive;

import java.util.List;

import lombok.Data;

@Data
public class ParametersDto {
    private String country; //ISO code IN
    private List<SavedAddresses> savedAddresses;
    private String displayText;
    private String url;
    private String flowMessageVersion;
    private String flowToken;
    private String flowId;
    private String flowCta;
    private String flowAction;
    private FlowActionPayload flowActionPayload;

}
