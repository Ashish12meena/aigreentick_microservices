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
import com.aigreentick.services.notification.kafka.producer.KafkaAuditProducer;
import com.aigreentick.services.notification.model.entity.EmailNotification;
import com.aigreentick.services.notification.provider.email.EmailProviderStrategy;
import com.aigreentick.services.notification.provider.selector.EmailProviderSelector;

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
    private final KafkaAuditProducer auditProducer;

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
            // Save notification to database
            emailNotificationService.save(notification);
            
            // Send audit event to Kafka asynchronously (fire-and-forget)
            long processingTime = System.currentTimeMillis() - startTime;
            sendAuditEventAsync(notification, eventId, processingTime, errorMessage);
        }
        
        return notification;
    }

    /**
     * Send audit event asynchronously without blocking main flow
     */
    private void sendAuditEventAsync(EmailNotification notification, 
                                      String eventId, 
                                      Long processingTime,
                                      String errorMessage) {
        try {
            auditProducer.sendEmailAuditEvent(notification, eventId, processingTime, errorMessage)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send audit event for notification: {}. " +
                                    "This is non-critical and won't affect email delivery.",
                                    notification.getId(), ex);
                        }
                    });
        } catch (Exception e) {
            // Log but don't fail the main operation
            log.error("Exception while sending audit event: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private EmailNotification deliverFallback(EmailNotificationRequest request,
                                               String eventId,
                                               Exception exception) {
        long startTime = System.currentTimeMillis();
        
        log.error("Email delivery failed after all retry attempts for: {}. Error: {}",
                request.getTo(),
                exception.getMessage());

        // Create failed notification record
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
        
        // Send audit event for failed delivery
        long processingTime = System.currentTimeMillis() - startTime;
        sendAuditEventAsync(saved, eventId, processingTime, exception.getMessage());
        
        return saved;
    }
}