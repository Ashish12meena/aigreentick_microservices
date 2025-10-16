package com.example.aigreentick.services.messaging.message.model.content;

import com.example.aigreentick.services.messaging.message.model.content.interactive.ActionDto;
import com.example.aigreentick.services.messaging.message.model.content.interactive.BodyDto;

import lombok.Data;

@Data
public class Interactive {
    private String type;
    private BodyDto body;
    private ActionDto action;
}
