package com.aigreentick.services.notification.model.entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.aigreentick.services.common.model.base.MongoBaseEntity;
import com.aigreentick.services.notification.enums.NotificationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Document(collection = "email_notification")
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotification extends MongoBaseEntity {
    private List<String> to;

    private String from;

    private List<String> cc;
    
    private List<String> bcc;

    private String subject;
    
    private String body;
    
    private NotificationStatus status;
    
    private String templateId;

    private List<String> attachmentUrls;

    private Integer retryCount;

    private String userId;

}
