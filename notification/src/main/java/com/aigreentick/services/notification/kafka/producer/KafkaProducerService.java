package com.aigreentick.services.notification.kafka.producer;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.aigreentick.services.notification.config.properties.KafkaTopicProperties;
import com.aigreentick.services.notification.kafka.event.EmailNotificationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, EmailNotificationEvent> emailNotificationKafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    /**
     * Send email notification event to Kafka
     */
    public CompletableFuture<SendResult<String, EmailNotificationEvent>> sendEmailNotification(
            EmailNotificationEvent event) {
        
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        
        String topicName = topicProperties.getTopics().getEmailNotification();
        String key = generateKey(event);
        
        log.info("Publishing email notification event to Kafka. Topic: {}, EventId: {}, Key: {}", 
                topicName, event.getEventId(), key);
        
        CompletableFuture<SendResult<String, EmailNotificationEvent>> future = 
                emailNotificationKafkaTemplate.send(topicName, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Email notification event published successfully. EventId: {}, Offset: {}", 
                        event.getEventId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish email notification event. EventId: {}", 
                        event.getEventId(), ex);
            }
        });
        
        return future;
    }

    /**
     * Send email notification to retry topic
     */
    public CompletableFuture<SendResult<String, EmailNotificationEvent>> sendToRetryTopic(
            EmailNotificationEvent event) {
        
        String topicName = topicProperties.getTopics().getEmailNotificationRetry();
        String key = generateKey(event);
        
        log.warn("Publishing email notification to retry topic. Topic: {}, EventId: {}, RetryCount: {}", 
                topicName, event.getEventId(), event.getRetryCount());
        
        CompletableFuture<SendResult<String, EmailNotificationEvent>> future = 
                emailNotificationKafkaTemplate.send(topicName, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Email notification sent to retry topic successfully. EventId: {}", 
                        event.getEventId());
            } else {
                log.error("Failed to send email notification to retry topic. EventId: {}", 
                        event.getEventId(), ex);
            }
        });
        
        return future;
    }

    /**
     * Send email notification to DLQ
     */
    public CompletableFuture<SendResult<String, EmailNotificationEvent>> sendToDlq(
            EmailNotificationEvent event, String errorMessage) {
        
        String topicName = topicProperties.getTopics().getEmailNotificationDlq();
        String key = generateKey(event);
        
        log.error("Publishing email notification to DLQ. Topic: {}, EventId: {}, Error: {}", 
                topicName, event.getEventId(), errorMessage);
        
        if (event.getMetadata() != null) {
            event.getMetadata().put("dlqReason", errorMessage);
            event.getMetadata().put("dlqTimestamp", LocalDateTime.now().toString());
        }
        
        CompletableFuture<SendResult<String, EmailNotificationEvent>> future = 
                emailNotificationKafkaTemplate.send(topicName, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Email notification sent to DLQ successfully. EventId: {}", 
                        event.getEventId());
            } else {
                log.error("CRITICAL: Failed to send email notification to DLQ. EventId: {}", 
                        event.getEventId(), ex);
            }
        });
        
        return future;
    }

    /**
     * Send successful email notification event
     */
    public void sendSuccessEvent(EmailNotificationEvent event, String notificationId) {
        String topicName = topicProperties.getTopics().getEmailNotificationSuccess();
        String key = generateKey(event);
        
        if (event.getMetadata() != null) {
            event.getMetadata().put("notificationId", notificationId);
            event.getMetadata().put("successTimestamp", LocalDateTime.now().toString());
        }
        
        log.info("Publishing success event. Topic: {}, EventId: {}", topicName, event.getEventId());
        
        emailNotificationKafkaTemplate.send(topicName, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish success event. EventId: {}", 
                                event.getEventId(), ex);
                    }
                });
    }

    /**
     * Send failed email notification event
     */
    public void sendFailedEvent(EmailNotificationEvent event, String errorMessage) {
        String topicName = topicProperties.getTopics().getEmailNotificationFailed();
        String key = generateKey(event);
        
        if (event.getMetadata() != null) {
            event.getMetadata().put("failureReason", errorMessage);
            event.getMetadata().put("failureTimestamp", LocalDateTime.now().toString());
        }
        
        log.warn("Publishing failed event. Topic: {}, EventId: {}", topicName, event.getEventId());
        
        emailNotificationKafkaTemplate.send(topicName, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish failed event. EventId: {}", 
                                event.getEventId(), ex);
                    }
                });
    }

    /**
     * Generate partition key for email notifications
     * Uses userId or first recipient email for consistent partitioning
     */
    private String generateKey(EmailNotificationEvent event) {
        if (event.getUserId() != null) {
            return event.getUserId();
        }
        
        if (event.getTo() != null && !event.getTo().isEmpty()) {
            return event.getTo().get(0);
        }
        
        return event.getEventId();
    }
}