// src/main/java/com/aigreentick/services/notification/service/email/impl/EmailDeliveryServiceImpl.java
package com.aigreentick.services.notification.service.email.impl;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aigreentick.services.notification.config.properties.EmailProperties;
import com.aigreentick.services.notification.dto.request.email.EmailNotificationRequest;
import com.aigreentick.services.notification.enums.NotificationStatus;
import com.aigreentick.services.notification.enums.email.EmailProviderType;
import com.aigreentick.services.notification.exceptions.NotificationSendException;
import com.aigreentick.services.notification.kafka.event.NotificationAuditEvent;
import com.aigreentick.services.notification.model.entity.EmailNotification;
import com.aigreentick.services.notification.provider.email.EmailProviderStrategy;
import com.aigreentick.services.notification.provider.selector.EmailProviderSelector;
import com.aigreentick.services.notification.service.batch.BatchNotificationWriter;
import com.aigreentick.services.notification.service.outbox.OutboxService;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDeliveryServiceImpl {
    private final EmailProviderSelector providerSelector;
    private final EmailNotificationServiceImpl emailNotificationService;
    private final EmailProperties emailProperties;
    private final OutboxService outboxService;
    private final BatchNotificationWriter batchWriter;

    @Transactional
    @Retry(name = "emailRetry", fallbackMethod = "deliverFallback")
    public EmailNotification deliver(EmailNotificationRequest request) {
        return deliver(request, null);
    }

    @Transactional
    @Retry(name = "emailRetry", fallbackMethod = "deliverFallback")
    public EmailNotification deliver(EmailNotificationRequest request, String eventId) {
        EmailProviderStrategy provider = providerSelector.selectProvider();
        return executeDelivery(request, provider, eventId);
    }

    @Async("emailTaskExecutor")
    public CompletableFuture<EmailNotification> deliverAsync(EmailNotificationRequest request) {
        log.info("Async email delivery started for: {}", request.getTo());
        try {
            EmailNotification result = deliver(request);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Async email delivery failed for: {}", request.getTo(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public EmailNotification deliverWithProvider(EmailNotificationRequest request, 
                                                   EmailProviderType providerType) {
        log.info("Delivering email with specific provider: {}", providerType);
        EmailProviderStrategy provider = providerSelector.getProvider(providerType);
        return executeDelivery(request, provider, null);
    }

    private EmailNotification createNotificationRecord(EmailNotificationRequest request,
                                                        EmailProviderType providerType) {
        return EmailNotification.builder()
                .to(request.getTo())
                .from(emailProperties.getFromEmail())
                .cc(request.getCc())
                .bcc(request.getBcc())
                .subject(request.getSubject())
                .body(request.getBody())
                .status(NotificationStatus.PROCESSING)
                .providerType(providerType)
                .retryCount(0)
                .build();
    }

    private EmailNotification executeDelivery(EmailNotificationRequest request, 
                                               EmailProviderStrategy provider,
                                               String eventId) {
        long startTime = System.currentTimeMillis();
        EmailNotification notification = createNotificationRecord(request, provider.getProviderType());
        String errorMessage = null;

        try {
            provider.send(request);

            notification.setStatus(NotificationStatus.SENT);
            notification.setCreatedAt(LocalDateTime.now());

            log.info("Email delivered successfully to: {} via {}",
                    request.getTo(),
                    provider.getProviderType());

        } catch (Exception e) {
            log.error("Failed to deliver email via provider: {}", provider.getProviderType(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setCreatedAt(LocalDateTime.now());
            errorMessage = e.getMessage();
            throw new NotificationSendException("Failed to deliver email", e);
        } finally {
            // Use batch writer for async persistence
            EmailNotification savedNotification = persistNotificationAsync(notification);
            
            // Create audit event in outbox (transactional with notification save)
            saveAuditEventToOutbox(savedNotification, eventId, 
                    System.currentTimeMillis() - startTime, errorMessage);
        }
        
        return notification;
    }

    /**
     * Persist notification using batch writer (non-blocking)
     */
    private EmailNotification persistNotificationAsync(EmailNotification notification) {
        try {
            // Try batch writer first
            boolean enqueued = batchWriter.enqueue(notification);
            
            if (!enqueued) {
                // Fallback to synchronous save if queue is full
                return emailNotificationService.save(notification);
            }
            
            return notification;
            
        } catch (Exception e) {
            log.error("Error enqueueing notification for batch write, saving synchronously", e);
            return emailNotificationService.save(notification);
        }
    }

    /**
     * Save audit event to outbox (async via scheduled publisher)
     */
    private void saveAuditEventToOutbox(EmailNotification notification, 
                                         String eventId, 
                                         Long processingTime,
                                         String errorMessage) {
        try {
            NotificationAuditEvent auditEvent = buildAuditEvent(
                    notification, eventId, processingTime, errorMessage);
            
            // Save to outbox - will be published asynchronously
            outboxService.saveAuditEvent(auditEvent);
            
        } catch (Exception e) {
            log.error("Error saving audit event to outbox for notification: {}. " +
                    "This is non-critical.", notification.getId(), e);
        }
    }

    private NotificationAuditEvent buildAuditEvent(EmailNotification notification,
                                                     String eventId,
                                                     Long processingTime,
                                                     String errorMessage) {
        return NotificationAuditEvent.builder()
                .notificationId(notification.getId())
                .eventId(eventId)
                .status(notification.getStatus())
                .providerType(notification.getProviderType() != null ? 
                        notification.getProviderType().name() : null)
                .recipient(notification.getTo() != null && !notification.getTo().isEmpty() ? 
                        notification.getTo().get(0) : null)
                .retryCount(notification.getRetryCount())
                .processingTimeMs(processingTime)
                .errorMessage(errorMessage)
                .userId(notification.getUserId())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @SuppressWarnings("unused")
    private EmailNotification deliverFallback(EmailNotificationRequest request,
                                               String eventId,
                                               Exception exception) {
        long startTime = System.currentTimeMillis();
        
        log.error("Email delivery failed after all retry attempts for: {}. Error: {}",
                request.getTo(),
                exception.getMessage());

        EmailNotification failedNotification = EmailNotification.builder()
                .to(request.getTo())
                .from(emailProperties.getFromEmail())
                .cc(request.getCc())
                .bcc(request.getBcc())
                .subject(request.getSubject())
                .body(request.getBody())
                .status(NotificationStatus.FAILED)
                .providerType(EmailProviderType.SMTP)
                .retryCount(emailProperties.getRetry().getMaxAttempts())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        EmailNotification saved = emailNotificationService.save(failedNotification);
        
        saveAuditEventToOutbox(saved, eventId, 
                System.currentTimeMillis() - startTime, exception.getMessage());
        
        return saved;
    }
}