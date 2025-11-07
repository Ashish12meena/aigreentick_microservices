package com.aigreentick.services.notification.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "resilience4j.circuitbreaker.instances.email-provider")
@Data
@Validated
public class EmailProviderCircuitBreakerProperties {
    
    @Min(1)
    @Max(100)
    private int slidingWindowSize = 10;
    
    @Min(1)
    @Max(20)
    private int minimumNumberOfCalls = 5;
    
    @Min(0)
    @Max(100)
    private int failureRateThreshold = 50;
    
    @Min(0)
    @Max(100)
    private int slowCallRateThreshold = 100;
    
    @Min(1000)
    private long slowCallDurationThresholdMs = 5000;
    
    @Min(1000)
    private long waitDurationInOpenStateMs = 30000;
    
    @Min(1)
    @Max(10)
    private int permittedNumberOfCallsInHalfOpenState = 3;
    
    private boolean automaticTransitionFromOpenToHalfOpenEnabled = true;
}
