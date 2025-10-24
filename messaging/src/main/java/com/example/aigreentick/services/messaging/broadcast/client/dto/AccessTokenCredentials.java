package com.example.aigreentick.services.messaging.broadcast.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccessTokenCredentials {
    private final String id; // WABA ID or PhoneNumber ID
    private final String accessToken;
}
