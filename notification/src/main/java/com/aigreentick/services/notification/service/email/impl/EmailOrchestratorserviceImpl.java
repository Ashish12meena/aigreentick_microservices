package com.aigreentick.services.notification.service.email.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aigreentick.services.notification.dto.request.email.EmailNotificationControllerRequest;
import com.aigreentick.services.notification.dto.request.email.EmailNotificationRequest;
import com.aigreentick.services.notification.dto.request.email.SendTemplatedEmailRequest;
import com.aigreentick.services.notification.dto.response.EmailNotificationResponse;
import com.aigreentick.services.notification.mapper.EmailNotificationMapper;
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
        private final EmailNotificationMapper emailNotificationMapper;

        public EmailNotificationResponse sendEmail(EmailNotificationControllerRequest request,
                        List<MultipartFile> attachmentFiles, List<MultipartFile> inlineResources) {
                log.info("Orchestrating email send to: {}", request.getTo());

                // Use mapper to convert
                EmailNotificationRequest emailNotificationRequest = emailNotificationMapper
                                .toEmailRequest(request, attachmentFiles,inlineResources);

                validationService.validateEmailRequest(emailNotificationRequest);
                EmailNotification notification = emailDeliveryService.deliver(emailNotificationRequest);
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
