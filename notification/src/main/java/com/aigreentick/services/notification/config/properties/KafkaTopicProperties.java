package com.aigreentick.services.notification.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "kafka")
@Data
@Validated
public class KafkaTopicProperties {
    
    private Topics topics = new Topics();
    
    @Min(1)
    private int partitions = 3;
    
    @Min(1)
    private int replicationFactor = 1;
    
    private RetryConfig retry = new RetryConfig();
    private DlqConfig dlq = new DlqConfig();
    
    @Data
    public static class Topics {
        @NotBlank
        private String emailNotification = "email.notification.topic";
        
        @NotBlank
        private String emailNotificationRetry = "email.notification.retry.topic";
        
        @NotBlank
        private String emailNotificationDlq = "email.notification.dlq.topic";
        
        @NotBlank
        private String emailNotificationSuccess = "email.notification.success.topic";
        
        @NotBlank
        private String emailNotificationFailed = "email.notification.failed.topic";
        
        @NotBlank
        private String pushNotification = "push.notification.topic";
        
        @NotBlank
        private String notificationAudit = "notification.audit.topic";
    }
    
    @Data
    public static class RetryConfig {
        @Min(1)
        private int maxAttempts = 3;
        
        @Min(100)
        private long backoffMs = 1000;
        
        @Min(1000)
        private long maxBackoffMs = 10000;
        
        @Min(1)
        private double multiplier = 2.0;
    }
    
    @Data
    public static class DlqConfig {
        private boolean enabled = true;
        
        @Min(60000) // At least 1 minute
        private long retentionMs = 604800000; // 7 days
    }
}