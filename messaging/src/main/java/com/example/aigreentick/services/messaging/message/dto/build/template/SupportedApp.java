package com.example.aigreentick.services.messaging.message.dto.build.template;

import lombok.Data;

@Data
public class SupportedApp {
    private String packageName;
    private String signatureHash;
}
