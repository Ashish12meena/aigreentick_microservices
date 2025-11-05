package com.aigreentick.services.notification.kafka.producer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.aigreentick.services.notification.config.properties.KafkaTopicProperties;
import com.aigreentick.services.notification.enums.NotificationChannel;
import com.aigreentick.services.notification.kafka.event.NotificationAuditEvent;
import com.aigreentick.services.notification.model.entity.EmailNotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaAuditProducer {

    private final KafkaTemplate<String, NotificationAuditEvent> auditKafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    /**
     * Send audit event for email notification processing
     */
    public CompletableFuture<SendResult<String, NotificationAuditEvent>> sendEmailAuditEvent(
            EmailNotification notification,
            String eventId,
            Long processingTimeMs,
            String errorMessage) {
        
        NotificationAuditEvent auditEvent = NotificationAuditEvent.builder()
                .auditId(UUID.randomUUID().toString())
                .notificationId(notification.getId())
                .eventId(eventId)
                .correlationId(eventId) // Can be enhanced with actual correlation ID
                .channel(NotificationChannel.EMAIL)
                .status(notification.getStatus())
                .providerType(notification.getProviderType() != null ? 
                        notification.getProviderType().name() : null)
                .recipient(notification.getTo() != null && !notification.getTo().isEmpty() ? 
                        notification.getTo().get(0) : null)
                .retryCount(notification.getRetryCount())
                .processingTimeMs(processingTimeMs)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .userId(notification.getUserId())
                .metadata(buildMetadata(notification))
                .build();

        return sendAuditEvent(auditEvent);
    }

    /**
     * Generic method to send audit events
     */
    public CompletableFuture<SendResult<String, NotificationAuditEvent>> sendAuditEvent(
            NotificationAuditEvent event) {
        
        String topicName = topicProperties.getTopics().getNotificationAudit();
        String key = generateKey(event);
        
        log.debug("Publishing audit event to Kafka. Topic: {}, AuditId: {}", 
                topicName, event.getAuditId());
        
        CompletableFuture<SendResult<String, NotificationAuditEvent>> future = 
                auditKafkaTemplate.send(topicName, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Audit event published successfully. AuditId: {}, Offset: {}", 
                        event.getAuditId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish audit event. AuditId: {}", 
                        event.getAuditId(), ex);
            }
        });
        
        return future;
    }

    /**
     * Build metadata map from notification
     */
    private Map<String, String> buildMetadata(EmailNotification notification) {
        Map<String, String> metadata = new HashMap<>();
        
        if (notification.getSubject() != null) {
            metadata.put("subject", notification.getSubject());
        }
        
        if (notification.getCc() != null && !notification.getCc().isEmpty()) {
            metadata.put("ccCount", String.valueOf(notification.getCc().size()));
        }
        
        if (notification.getBcc() != null && !notification.getBcc().isEmpty()) {
            metadata.put("bccCount", String.valueOf(notification.getBcc().size()));
        }
        
        if (notification.getAttachmentUrls() != null && !notification.getAttachmentUrls().isEmpty()) {
            metadata.put("attachmentCount", String.valueOf(notification.getAttachmentUrls().size()));
        }
        
        return metadata;
    }

    /**
     * Generate partition key for audit events
     */
    private String generateKey(NotificationAuditEvent event) {
        if (event.getUserId() != null) {
            return event.getUserId();
        }
        
        if (event.getNotificationId() != null) {
            return event.getNotificationId();
        }
        
        return event.getAuditId();
    }
}