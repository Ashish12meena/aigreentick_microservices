
package com.aigreentick.services.notification.model.entity;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aigreentick.services.common.model.base.MongoBaseEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity to store Dead Letter Queue messages for manual processing
 */
@Document(collection = "dlq_message")
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DlqMessage extends MongoBaseEntity {
    
    @Indexed(unique = true)
    private String eventId;
    
    private String originalTopic;
    
    private String dlqTopic;
    
    private Integer partition;
    
    private Long offset;
    
    private String payload; // JSON string
    
    private Integer retryCount;
    
    private String errorReason;
    
    @Indexed
    private boolean processed;
    
    private String reprocessedBy;
    
    private String reprocessingNotes;
}