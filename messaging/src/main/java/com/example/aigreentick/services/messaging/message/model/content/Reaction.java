package com.example.aigreentick.services.messaging.message.model.content;

import lombok.Data;

@Data
public class Reaction {
    private String messageId;
    private String emoji;
}
