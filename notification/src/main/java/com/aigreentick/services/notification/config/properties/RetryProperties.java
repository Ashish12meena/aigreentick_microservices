package com.aigreentick.services.notification.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "email.retry")
@Data
@Validated
public class RetryProperties {
    
    private boolean enabled = true;
    
    @Min(1)
    @Max(10)
    private int maxAttempts = 3;
    
    @Min(100)
    private long initialDelayMs = 2000;
    
    @Min(1)
    private double multiplier = 2.0;
    
    @Min(1000)
    private long maxDelayMs = 60000;
}