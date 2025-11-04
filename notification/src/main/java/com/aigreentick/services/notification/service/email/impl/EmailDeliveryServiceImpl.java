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

    @Transactional
    @Retry(name = "emailRetry", fallbackMethod = "deliverFallback")
    public EmailNotification deliver(EmailNotificationRequest request) {
        EmailProviderStrategy provider = providerSelector.selectProvider();

        return executeDelivery(request, provider);

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

    public EmailNotification deliverWithProvider(EmailNotificationRequest request, EmailProviderType providerType) {
        log.info("Delivering email with specific provider: {}", providerType);

        EmailProviderStrategy provider = providerSelector.getProvider(providerType);

        return executeDelivery(request, provider);

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

    private EmailNotification executeDelivery(EmailNotificationRequest request, EmailProviderStrategy provider) {
        EmailNotification notification = createNotificationRecord(request, provider.getProviderType());

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
            throw new NotificationSendException("Failed to deliver email", e);
        } finally {

            emailNotificationService.save(notification);
        }
        return notification;
    }

    @SuppressWarnings("unused")
    private EmailNotification deliverFallback(
            EmailNotificationRequest request,
            Exception exception) {

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

        return emailNotificationService.save(failedNotification);
    }
}
