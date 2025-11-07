package com.aigreentick.services.notification.kafka.event;


import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.aigreentick.services.notification.enums.email.EmailPriority;

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
    
    private Instant timestamp;
    
    private Map<String, String> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentData {
        private String filename;
        private byte[] content;
        private String contentType;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InlineResourceData {
        private String contentId;
        private byte[] content;
        private String contentType;
    }
}