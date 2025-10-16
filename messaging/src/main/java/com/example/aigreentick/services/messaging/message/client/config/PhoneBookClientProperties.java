package com.example.aigreentick.services.messaging.message.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "phone-book-service")
@Data
public class PhoneBookClientProperties {
    private String baseUrl;
    private String apiVersion;

    // Feature flags for dynamic enable/disable
    private volatile boolean outgoingEnabled = true;
}
