package com.aigreentick.services.notification.service.email.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aigreentick.services.notification.dto.request.email.EmailNotificationControllerRequest;
import com.aigreentick.services.notification.dto.request.email.EmailNotificationRequest;
import com.aigreentick.services.notification.dto.request.email.SendTemplatedEmailRequest;
import com.aigreentick.services.notification.dto.response.EmailNotificationResponse;
import com.aigreentick.services.notification.kafka.event.EmailNotificationEvent;
import com.aigreentick.services.notification.kafka.producer.KafkaProducerService;
import com.aigreentick.services.notification.mapper.EmailEventMapper;
import com.aigreentick.services.notification.mapper.EmailNotificationMapper;
import com.aigreentick.services.notification.model.entity.EmailNotification;
import com.aigreentick.services.notification.validator.EmailValidationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailOrchestratorServiceImpl {
    private final EmailDeliveryServiceImpl emailDeliveryService;
    private final EmailTemplateProcessorService templateProcessor;
    private final EmailValidationService validationService;
    private final EmailNotificationMapper emailNotificationMapper;
    private final KafkaProducerService kafkaProducerService;
    private final EmailEventMapper emailEventMapper;

    // ==================== Synchronous Email Sending` ====================

    public EmailNotificationResponse sendEmail(EmailNotificationControllerRequest request,
                                                List<MultipartFile> attachmentFiles, 
                                                List<MultipartFile> inlineResources) {
        log.info("Orchestrating email send to: {}", request.getTo());

        EmailNotificationRequest emailNotificationRequest = emailNotificationMapper
                .toEmailRequest(request, attachmentFiles, inlineResources);

        validationService.validateEmailRequest(emailNotificationRequest);
        EmailNotification notification = emailDeliveryService.deliver(emailNotificationRequest);
        return mapToResponse(notification);
    }

    // ==================== Asynchronous Email Sending ====================

    public CompletableFuture<EmailNotificationResponse> sendEmailAsync(
            EmailNotificationRequest request) {
        log.info("Orchestrating async email send to: {}", request.getTo());

        validationService.validateEmailRequest(request);

        return emailDeliveryService.deliverAsync(request)
                .thenApply(this::mapToResponse);
    }

    // ==================== Kafka-Based Email Sending ====================

    public CompletableFuture<String> sendEmailViaKafka(
            EmailNotificationRequest request,
            String userId,
            String sourceService) {
        
        log.info("Orchestrating Kafka-based email send to: {}", request.getTo());

        // Validate request
        validationService.validateEmailRequest(request);

        // Convert to Kafka event
        EmailNotificationEvent event = emailEventMapper.toEvent(request, userId, sourceService);

        // Send to Kafka and return eventId
        return kafkaProducerService.sendEmailNotification(event)
                .thenApply(result -> {
                    log.info("Email notification queued successfully. EventId: {}", event.getEventId());
                    return event.getEventId();
                })
                .exceptionally(ex -> {
                    log.error("Failed to queue email notification", ex);
                    throw new RuntimeException("Failed to queue email notification", ex);
                });
    }

    // ==================== Templated Email Sending ====================

    public EmailNotificationResponse sendTemplatedEmail(SendTemplatedEmailRequest request) {
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

    public CompletableFuture<String> sendTemplatedEmailViaKafka(
            SendTemplatedEmailRequest request,
            String userId) {
        
        log.info("Orchestrating Kafka-based templated email to: {} with template: {}",
                request.getTo(), request.getTemplateCode());

        // Create base event
        EmailNotificationEvent event = EmailNotificationEvent.builder()
                .to(request.getTo())
                .cc(request.getCc())
                .bcc(request.getBcc())
                .templateCode(request.getTemplateCode())
                .templateVariables(request.getVariables())
                .userId(userId)
                .sourceService("notification-service")
                .retryCount(0)
                .build();

        // Add attachments if present
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            event.setAttachments(request.getAttachments().stream()
                    .map(att -> EmailNotificationEvent.AttachmentData.builder()
                            .filename(att.getFilename())
                            .content(att.getContent())
                            .contentType(att.getContentType())
                            .build())
                    .toList());
        }

        // Add metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("templateBased", "true");
        metadata.put("templateCode", request.getTemplateCode());
        event.setMetadata(metadata);

        // Send to Kafka
        return kafkaProducerService.sendEmailNotification(event)
                .thenApply(result -> {
                    log.info("Templated email notification queued successfully. EventId: {}", 
                            event.getEventId());
                    return event.getEventId();
                })
                .exceptionally(ex -> {
                    log.error("Failed to queue templated email notification", ex);
                    throw new RuntimeException("Failed to queue templated email notification", ex);
                });
    }

    // ==================== Helper Methods ====================

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