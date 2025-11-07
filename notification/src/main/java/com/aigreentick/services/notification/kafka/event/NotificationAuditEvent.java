package com.aigreentick.services.notification.kafka.event;

import java.time.Instant;
import java.util.Map;

import com.aigreentick.services.notification.enums.NotificationChannel;
import com.aigreentick.services.notification.enums.NotificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAuditEvent {
    
    private String auditId;
    
    private String notificationId;
    
    private String eventId;
    
    private String correlationId;
    
    private NotificationChannel channel;
    
    private NotificationStatus status;
    
    private String providerType;
    
    private String recipient;
    
    private Integer retryCount;
    
    private Long processingTimeMs;
    
    private String errorMessage;
    
    private String errorCode;
    
    private Instant timestamp;
    
    private String userId;
    
    private String sourceService;
    
    private Map<String, String> metadata;
}
