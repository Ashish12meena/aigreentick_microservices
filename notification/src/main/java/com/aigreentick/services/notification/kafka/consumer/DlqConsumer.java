// src/main/java/com/aigreentick/services/notification/kafka/consumer/DlqConsumer.java
package com.aigreentick.services.notification.kafka.consumer;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.aigreentick.services.notification.config.properties.KafkaTopicProperties;
import com.aigreentick.services.notification.kafka.event.EmailNotificationEvent;
import com.aigreentick.services.notification.model.entity.DlqMessage;
import com.aigreentick.services.notification.repository.DlqMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumer for Dead Letter Queue messages
 * Stores failed messages for manual review and retry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DlqConsumer {

    private final DlqMessageRepository dlqRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTopicProperties topicProperties; // ← ADDED: Missing dependency

    @KafkaListener(
            topics = "#{kafkaTopicProperties.topics.emailNotificationDlq}",
            groupId = "${spring.kafka.consumer.group-id}-dlq",
            containerFactory = "retryKafkaListenerContainerFactory"
    )
    public void consumeDlqMessage(
            @Payload EmailNotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.warn("Received message in DLQ. Topic: {}, Partition: {}, Offset: {}, EventId: {}",
                topic, partition, offset, event.getEventId());

        try {
            // Store DLQ message for manual processing
            DlqMessage dlqMessage = DlqMessage.builder()
                    .eventId(event.getEventId())
                    .originalTopic(topicProperties.getTopics().getEmailNotification()) // ← FIXED: Now works
                    .dlqTopic(topic)
                    .partition(partition)
                    .offset(offset)
                    .payload(objectMapper.writeValueAsString(event))
                    .retryCount(event.getRetryCount())
                    .errorReason(event.getMetadata() != null ? 
                            event.getMetadata().get("dlqReason") : "Unknown")
                    .processed(false)
                    .createdAt(LocalDateTime.now()) // ← ADDED: Set creation timestamp
                    .build();

            dlqRepository.save(dlqMessage);
            
            log.info("DLQ message stored successfully. EventId: {}", event.getEventId());
            
            // Send alert notification (email/Slack) for critical failures
            sendDlqAlert(event);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing DLQ message. EventId: {}", event.getEventId(), e);
            // Still acknowledge to prevent infinite loop
            acknowledgment.acknowledge();
        }
    }

    private void sendDlqAlert(EmailNotificationEvent event) {
        try {
            log.error("ALERT: Message sent to DLQ. EventId: {}, To: {}, RetryCount: {}", 
                    event.getEventId(), 
                    event.getTo(), 
                    event.getRetryCount());
            
        
            
        } catch (Exception e) {
            log.error("Error sending DLQ alert", e);
        }
    }
    
    /**
     * Helper method to build alert message (optional)
     */
    @SuppressWarnings("unused")
    private String buildAlertMessage(EmailNotificationEvent event) {
        return String.format(
            "⚠️ Email Failed - Sent to DLQ%n" +
            "EventId: %s%n" +
            "Recipients: %s%n" +
            "Retry Count: %d%n" +
            "Timestamp: %s%n" +
            "Reason: %s",
            event.getEventId(),
            event.getTo(),
            event.getRetryCount(),
            event.getTimestamp(),
            event.getMetadata() != null ? event.getMetadata().get("dlqReason") : "Unknown"
        );
    }
}