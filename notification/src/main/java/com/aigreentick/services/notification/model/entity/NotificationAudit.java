package com.aigreentick.services.notification.model.entity;

import java.time.LocalDateTime;
import java.util.Map;


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
    
    private String auditId;
    
    private String notificationId;
    
    private String eventId;
    
    private NotificationChannel channel;
    
    private NotificationStatus status;
    
    private String providerType;
    
    private String recipient;
    
    private Integer retryCount;
    
    private Long processingTimeMs;
    
    private String errorMessage;
    
    private String errorCode;
    
    private LocalDateTime timestamp;
    
    private String userId;
    
    
    private Map<String, String> metadata;
}