package com.aigreentick.services.notification.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterProperties;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "email")
@Data
public class EmailProperties {
    private String fromEmail=  "jahanvinwork@gmail.com";

    private String name = "AI GreenTick";

    private String encoding = "UTF-8";

    private RetryProperties retry;
    private EmailTemplateProperties template;
    private RateLimiterProperties rateLimit;
    private ValidationProperties validation;
    private AttachmentProperties attachments;


    @Data
    public static class RateLimitProperties {
        private boolean enabled = true;
        private LimitConfig global = new LimitConfig(1000, 1500);
        private LimitConfig perUser = new LimitConfig(60, 100);
        private LimitConfig perIp = new LimitConfig(100, 150);
    }

     @Data
    public static class LimitConfig {
        @Min(1)
        private int requestsPerMinute;
        
        @Min(1)
        private int burstCapacity;
        
        public LimitConfig() {}
        
        public LimitConfig(int requestsPerMinute, int burstCapacity) {
            this.requestsPerMinute = requestsPerMinute;
            this.burstCapacity = burstCapacity;
        }
    }

    @Data
    public static class ValidationProperties {
        private boolean enabled = true;
        private boolean checkMxRecords = false;
        private boolean disposableDomainsCheck = true;
        
        @Min(1)
        @Max(100)
        private int maxRecipients = 50;
        
        @Min(1)
        @Max(50)
        private int maxCcRecipients = 20;
        
        @Min(1)
        @Max(100)
        private int maxBccRecipients = 50;
        
        @Min(1)
        @Max(20)
        private int maxAttachments = 10;
        
        @Min(1)
        @Max(100)
        private int maxAttachmentSizeMb = 25;
        
        @Min(1)
        @Max(2048)
        private int maxBodySizeKb = 500;
    }

    @Data
    public static class AttachmentProperties {
        private String allowedTypes = "pdf,doc,docx,xls,xlsx,jpg,jpeg,png,gif,txt,csv";
        
        @Min(1)
        @Max(50)
        private int maxSizePerFileMb = 10;
        
        @Min(1)
        @Max(100)
        private int maxTotalSizeMb = 25;
    }

    
}
