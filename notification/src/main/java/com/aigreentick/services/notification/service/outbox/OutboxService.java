// src/main/java/com/aigreentick/services/notification/service/outbox/OutboxService.java
package com.aigreentick.services.notification.service.outbox;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aigreentick.services.notification.enums.OutboxEventStatus;
import com.aigreentick.services.notification.kafka.event.NotificationAuditEvent;
import com.aigreentick.services.notification.model.entity.OutboxEvent;
import com.aigreentick.services.notification.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Outbox pattern service for transactional event publishing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {
    
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Save audit event to outbox within same transaction as notification
     */
    @Transactional
    public OutboxEvent saveAuditEvent(NotificationAuditEvent auditEvent) {
        try {
            String payload = objectMapper.writeValueAsString(auditEvent);
            
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(auditEvent.getAuditId())
                    .aggregateType("NOTIFICATION_AUDIT")
                    .aggregateId(auditEvent.getNotificationId())
                    .eventType("AUDIT_EVENT")
                    .payload(payload)
                    .status(OutboxEventStatus.PENDING)
                    .retryCount(0)
                    .createdAt(Instant.now())
                    .build();
            
            OutboxEvent saved = outboxRepository.save(outboxEvent);
            log.debug("Saved audit event to outbox: {}", saved.getEventId());
            
            return saved;
            
        } catch (JsonProcessingException e) {
            log.error("Error serializing audit event to outbox", e);
            throw new RuntimeException("Failed to save audit event to outbox", e);
        }
    }
    
    /**
     * Get pending events for publishing
     */
    @Transactional(readOnly = true)
    public List<OutboxEvent> getPendingEvents() {
        return outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);
    }
    
    /**
     * Mark event as published
     */
    @Transactional
    public void markAsPublished(String eventId) {
        outboxRepository.findById(eventId).ifPresent(event -> {
            event.setStatus(OutboxEventStatus.PUBLISHED);
            event.setUpdatedAt(Instant.now());
            outboxRepository.save(event);
            log.debug("Marked outbox event as published: {}", eventId);
        });
    }
    
    /**
     * Mark event as failed
     */
    @Transactional
    public void markAsFailed(String eventId, String errorMessage) {
        outboxRepository.findById(eventId).ifPresent(event -> {
            event.setStatus(OutboxEventStatus.FAILED);
            event.setRetryCount(event.getRetryCount() + 1);
            event.setErrorMessage(errorMessage);
            event.setUpdatedAt(Instant.now());
            outboxRepository.save(event);
            log.warn("Marked outbox event as failed: {}", eventId);
        });
    }
}