package com.aigreentick.services.notification.kafka.event;

import java.time.LocalDateTime;
import java.util.Map;

import com.aigreentick.services.notification.enums.NotificationChannel;
import com.aigreentick.services.notification.enums.NotificationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

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
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String userId;
    
    private String sourceService;
    
    private Map<String, String> metadata;
}
