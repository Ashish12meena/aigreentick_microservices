package com.example.aigreentick.services.messaging.message.model;

import lombok.Data;

@Data
public class Context {
    private String messageId;
    private boolean forwarded;

}
