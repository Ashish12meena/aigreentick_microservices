// src/main/java/com/aigreentick/services/notification/scheduler/OutboxEventPublisher.java
package com.aigreentick.services.notification.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aigreentick.services.notification.kafka.event.NotificationAuditEvent;
import com.aigreentick.services.notification.kafka.producer.KafkaAuditProducer;
import com.aigreentick.services.notification.model.entity.OutboxEvent;
import com.aigreentick.services.notification.service.outbox.OutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled job to publish pending outbox events to Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    
    private final OutboxService outboxService;
    private final KafkaAuditProducer kafkaAuditProducer;
    private final ObjectMapper objectMapper;
    
    /**
     * Poll outbox every 5 seconds and publish pending events
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void publishPendingEvents() {
        try {
            List<OutboxEvent> pendingEvents = outboxService.getPendingEvents();
            
            if (pendingEvents.isEmpty()) {
                return;
            }
            
            log.info("Publishing {} pending outbox events", pendingEvents.size());
            
            for (OutboxEvent event : pendingEvents) {
                try {
                    publishEvent(event);
                } catch (Exception e) {
                    log.error("Error publishing outbox event: {}", event.getEventId(), e);
                    outboxService.markAsFailed(event.getEventId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error in outbox event publisher", e);
        }
    }
    
    private void publishEvent(OutboxEvent event) {
        try {
            // Deserialize and publish
            NotificationAuditEvent auditEvent = objectMapper.readValue(
                    event.getPayload(), 
                    NotificationAuditEvent.class);
            
            kafkaAuditProducer.sendAuditEvent(auditEvent)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            outboxService.markAsPublished(event.getEventId());
                            log.debug("Published outbox event: {}", event.getEventId());
                        } else {
                            outboxService.markAsFailed(event.getEventId(), ex.getMessage());
                            log.error("Failed to publish outbox event: {}", event.getEventId(), ex);
                        }
                    });
            
        } catch (Exception e) {
            log.error("Error processing outbox event: {}", event.getEventId(), e);
            throw new RuntimeException("Failed to publish outbox event", e);
        }
    }
}