package com.example.aigreentick.services.messaging.message.client.dto;

import java.util.Map;

import lombok.Data;

@Data
public class PhoneBookResponseDto {
    private Map<String, Map<String, String>> data;
}
