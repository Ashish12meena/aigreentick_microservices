package com.example.aigreentick.services.messaging.message.model.content.interactive;

import lombok.Data;

@Data
public class FlowActionPayload {
    private String screen;
    private FlowData data;
}
