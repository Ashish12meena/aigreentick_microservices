// src/main/java/com/aigreentick/services/notification/model/entity/OutboxEvent.java
package com.aigreentick.services.notification.model.entity;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aigreentick.services.common.model.base.MongoBaseEntity;
import com.aigreentick.services.notification.enums.OutboxEventStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Outbox pattern entity for transactional message publishing
 */
@Document(collection = "outbox_event")
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent extends MongoBaseEntity {
    
    @Indexed(unique = true)
    private String eventId;
    
    private String aggregateType; // e.g., "EMAIL_NOTIFICATION"
    
    private String aggregateId; // e.g., notification ID
    
    private String eventType; // e.g., "EMAIL_SENT", "EMAIL_FAILED"
    
    private String payload; // JSON payload
    
    @Indexed
    private OutboxEventStatus status;
    
    private Integer retryCount;
    
    private String errorMessage;
}