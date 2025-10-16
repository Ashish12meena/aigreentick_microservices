package com.aigreentick.services.template.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "template.service")
public class TemplateServiceProperties {
    private volatile boolean incomingEnabled = true;

    public boolean isIncomingEnabled() {
        return incomingEnabled;
    }

    public void setIncomingEnabled(boolean incomingEnabled) {
        this.incomingEnabled = incomingEnabled;
    }
}
