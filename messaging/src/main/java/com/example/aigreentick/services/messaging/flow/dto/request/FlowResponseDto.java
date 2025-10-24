package com.example.aigreentick.services.messaging.flow.dto.request;


import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;


@Data
public class FlowResponseDto {
    private String version;
    private String type;
    private JsonNode data;
}
