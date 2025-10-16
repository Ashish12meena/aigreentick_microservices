package com.example.aigreentick.services.messaging.flow.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;


@Data
public class FlowRequestDto {
    private String version;
    private String action;
    private String screen;
    private JsonNode data;
    private String flowToken;
    private String flowId; // optional: if you use flow ids
}
