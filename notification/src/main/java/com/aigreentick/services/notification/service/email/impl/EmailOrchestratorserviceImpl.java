package com.aigreentick.services.notification.service.email.impl;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.aigreentick.services.notification.dto.request.email.EmailNotificationRequest;
import com.aigreentick.services.notification.dto.request.email.SendTemplatedEmailRequest;
import com.aigreentick.services.notification.dto.response.EmailNotificationResponse;
import com.aigreentick.services.notification.model.entity.EmailNotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailOrchestratorserviceImpl {
    private final EmailDeliveryServiceImpl emailDeliveryService;
    private final EmailTemplateProcessorService templateProcessor;
    private final EmailValidationService validationService;

    public EmailNotificationResponse sendEmail(EmailNotificationRequest request) {
        log.info("Orchestrating email send to: {}", request.getTo());

        validationService.validateEmailRequest(request);
        EmailNotification notification = emailDeliveryService.deliver(request);
        return mapToResponse(notification);
    }

    public CompletableFuture<EmailNotificationResponse> sendEmailAsync(
            EmailNotificationRequest request) {
        log.info("Orchestrating async email send to: {}", request.getTo());

        validationService.validateEmailRequest(request);

        return emailDeliveryService.deliverAsync(request)
                .thenApply(this::mapToResponse);
    }

    public EmailNotificationResponse sendTemplatedEmail(
            SendTemplatedEmailRequest request) {
        log.info("Orchestrating templated email send to: {} with template: {}",
                request.getTo(), request.getTemplateCode());

        EmailNotificationRequest baseRequest = EmailNotificationRequest.builder()
                .to(request.getTo())
                .cc(request.getCc())
                .bcc(request.getBcc())
                .attachments(request.getAttachments())
                .build();

        EmailNotificationRequest processedRequest = templateProcessor
                .processTemplateByCode(
                        request.getTemplateCode(),
                        request.getVariables(),
                        baseRequest);

        validationService.validateEmailRequest(processedRequest);

        EmailNotification notification = emailDeliveryService.deliver(processedRequest);
        return mapToResponse(notification);
    }


    private EmailNotificationResponse mapToResponse(EmailNotification notification) {
        return EmailNotificationResponse.builder()
                .id(notification.getId())
                .to(notification.getTo())
                .subject(notification.getSubject())
                .status(notification.getStatus())
                .providerType(notification.getProviderType())
                .retryCount(notification.getRetryCount())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

   
}
