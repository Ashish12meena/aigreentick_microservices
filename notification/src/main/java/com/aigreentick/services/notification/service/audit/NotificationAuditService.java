package com.aigreentick.services.notification.service.audit;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.aigreentick.services.notification.kafka.event.NotificationAuditEvent;
import com.aigreentick.services.notification.model.entity.NotificationAudit;
import com.aigreentick.services.notification.repository.NotificationAuditRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationAuditService {

    private final NotificationAuditRepository auditRepository;

    /**
     * Save audit event to database
     */
    public void saveAuditEvent(NotificationAuditEvent event) {
        log.debug("Saving audit event. AuditId: {}, NotificationId: {}",
                event.getAuditId(), event.getNotificationId());

        NotificationAudit audit = NotificationAudit.builder()
                .auditId(event.getAuditId())
                .notificationId(event.getNotificationId())
                .eventId(event.getEventId())
                .correlationId(event.getCorrelationId())
                .channel(event.getChannel())
                .status(event.getStatus())
                .providerType(event.getProviderType())
                .recipient(event.getRecipient())
                .retryCount(event.getRetryCount())
                .processingTimeMs(event.getProcessingTimeMs())
                .errorMessage(event.getErrorMessage())
                .errorCode(event.getErrorCode())
                .timestamp(event.getTimestamp())
                .userId(event.getUserId())
                .sourceService(event.getSourceService())
                .metadata(event.getMetadata())
                .build();

        auditRepository.save(audit);
        log.info("Audit event saved successfully. AuditId: {}", event.getAuditId());
    }

    /**
     * Get audit events by notification ID
     */
    @Cacheable(value = "auditEvents", key = "#notificationId")
    public Page<NotificationAudit> getAuditsByNotificationId(String notificationId, Pageable pageable) {
        log.debug("Fetching audit events for notificationId: {}", notificationId);
        return auditRepository.findByNotificationId(notificationId, pageable);
    }

    /**
     * Get audit events by user ID
     */
    public Page<NotificationAudit> getAuditsByUserId(String userId, Pageable pageable) {
        log.debug("Fetching audit events for userId: {}", userId);
        return auditRepository.findByUserId(userId, pageable);
    }

    /**
     * Get audit events by correlation ID
     */
    public Page<NotificationAudit> getAuditsByCorrelationId(String correlationId, Pageable pageable) {
        log.debug("Fetching audit events for correlationId: {}", correlationId);
        return auditRepository.findByCorrelationId(correlationId, pageable);
    }
}