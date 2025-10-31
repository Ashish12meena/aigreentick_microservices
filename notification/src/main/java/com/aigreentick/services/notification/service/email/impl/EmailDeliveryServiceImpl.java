package com.aigreentick.services.notification.service.email.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.aigreentick.services.notification.dto.request.email.EmailNotificationRequest;
import com.aigreentick.services.notification.enums.NotificationStatus;
import com.aigreentick.services.notification.enums.email.EmailProviderType;
import com.aigreentick.services.notification.exceptions.NotificationSendException;
import com.aigreentick.services.notification.model.entity.EmailNotification;
import com.aigreentick.services.notification.provider.email.EmailProviderStrategy;
import com.aigreentick.services.notification.provider.selector.EmailProviderSelector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDeliveryServiceImpl {
    private final EmailProviderSelector providerSelector;
    private final EmailNotificationServiceImpl emailNotificationService;

    public EmailNotification deliver(EmailNotificationRequest request) {
        EmailProviderStrategy provider = providerSelector.selectProvider();

        EmailNotification notification = createNotificationRecord(request, provider.getProviderType());

        try {
            provider.send(request);

            notification.setStatus(NotificationStatus.SENT);
            notification.setCreatedAt(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Failed to deliver email via provider: {}", provider.getProviderType(), e);
            notification.setStatus(NotificationStatus.FAILED);
            throw new NotificationSendException("Failed to deliver email", e);
        } finally {

            emailNotificationService.save(notification);
        }
        return notification;
    }

    public EmailNotification deliverWithProvider(EmailNotificationRequest request, EmailProviderType providerType) {
        log.info("Delivering email with specific provider: {}", providerType);

        EmailProviderStrategy provider = providerSelector.getProvider(providerType);

        EmailNotification notification = createNotificationRecord(request, providerType);

         try {
            provider.send(request);

            notification.setStatus(NotificationStatus.SENT);
            notification.setCreatedAt(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Failed to deliver email via provider: {}", provider.getProviderType(), e);
            notification.setStatus(NotificationStatus.FAILED);
            throw new NotificationSendException("Failed to deliver email", e);
        } finally {

            emailNotificationService.save(notification);
        }

        return notification;

    }

    private EmailNotification createNotificationRecord(EmailNotificationRequest request,
            EmailProviderType providerType) {
        return EmailNotification.builder()
                .to(request.getTo())
                .cc(request.getCc())
                .bcc(request.getBcc())
                .subject(request.getSubject())
                .body(request.getBody())
                .status(NotificationStatus.PROCESSING)
                .providerType(providerType)
                .retryCount(0)
                .build();
    }
}
