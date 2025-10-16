package com.aigreentick.services.template.client.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccessTokenCredentials {
    private final String id; // WABA ID or PhoneNumber ID
    private final String accessToken;
}
