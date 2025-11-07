package com.aigreentick.services.notification.kafka.event;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.aigreentick.services.notification.enums.email.EmailPriority;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationEvent {
    
    private String eventId;
    
    private String correlationId;
    
    private List<String> to;
    
    private List<String> cc;
    
    private List<String> bcc;
    
    private String subject;
    
    private String body;
    
    private boolean isHtml;
    
    private EmailPriority priority;
    
    private String templateCode;
    
    private Map<String, Object> templateVariables;
    
    private List<AttachmentData> attachments;
    
    private List<InlineResourceData> inlineResources;
    
    private Integer retryCount;
    
    private String userId;
    
    private String sourceService;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Map<String, String> metadata;
  
}