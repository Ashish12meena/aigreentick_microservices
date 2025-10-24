package com.example.aigreentick.services.messaging.message.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "message.service")
public class MessageServiceProperties {
    private volatile boolean incomingEnabled = true;

    public boolean isIncomingEnabled() {
        return incomingEnabled;
    }

    public void setIncomingEnabled(boolean incomingEnabled) {
        this.incomingEnabled = incomingEnabled;
    }
}
