package com.aigreentick.services.notification.model.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aigreentick.services.common.model.base.MongoBaseEntity;
import com.aigreentick.services.notification.enums.NotificationChannel;
import com.aigreentick.services.notification.enums.NotificationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Document(collection = "notification_audit")
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAudit extends MongoBaseEntity {
    
    @Indexed
    private String auditId;
    
    @Indexed
    private String notificationId;
    
    @Indexed
    private String eventId;
    
    @Indexed
    private String correlationId;
    
    private NotificationChannel channel;
    
    private NotificationStatus status;
    
    private String providerType;
    
    private String recipient;
    
    private Integer retryCount;
    
    private Long processingTimeMs;
    
    private String errorMessage;
    
    private String errorCode;
    
    @Indexed
    private LocalDateTime timestamp;
    
    @Indexed
    private String userId;
    
    private String sourceService;
    
    private Map<String, String> metadata;
}