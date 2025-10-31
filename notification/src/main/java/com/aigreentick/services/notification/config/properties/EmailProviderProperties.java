package com.aigreentick.services.notification.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.aigreentick.services.notification.enums.email.EmailProviderType;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "email.provider")
@Data
public class EmailProviderProperties {
    private EmailProviderType active = EmailProviderType.SMTP;
}
