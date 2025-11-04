package com.aigreentick.services.notification.mapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.aigreentick.services.notification.dto.request.email.EmailAttachment;
import com.aigreentick.services.notification.dto.request.email.EmailNotificationRequest;
import com.aigreentick.services.notification.dto.request.email.InlineResource;
import com.aigreentick.services.notification.kafka.event.EmailNotificationEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailEventMapper {

    /**
     * Convert EmailNotificationRequest to Kafka Event
     */
    public EmailNotificationEvent toEvent(EmailNotificationRequest request) {
        return toEvent(request, null, null);
    }

    /**
     * Convert EmailNotificationRequest to Kafka Event with metadata
     */
    public EmailNotificationEvent toEvent(
            EmailNotificationRequest request, 
            String userId, 
            String sourceService) {
        
        log.debug("Mapping EmailNotificationRequest to EmailNotificationEvent");

        return EmailNotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .to(request.getTo())
                .cc(request.getCc())
                .bcc(request.getBcc())
                .subject(request.getSubject())
                .body(request.getBody())
                .isHtml(request.isHtml())
                .priority(request.getPriority())
                .attachments(mapAttachments(request.getAttachments()))
                .inlineResources(mapInlineResources(request.getInlineResources()))
                .retryCount(0)
                .userId(userId)
                .sourceService(sourceService != null ? sourceService : "notification-service")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Convert Kafka Event to EmailNotificationRequest
     */
    public EmailNotificationRequest toEmailRequest(EmailNotificationEvent event) {
        log.debug("Mapping EmailNotificationEvent to EmailNotificationRequest. EventId: {}", 
                event.getEventId());

        return EmailNotificationRequest.builder()
                .to(event.getTo())
                .cc(event.getCc())
                .bcc(event.getBcc())
                .subject(event.getSubject())
                .body(event.getBody())
                .isHtml(event.isHtml())
                .priority(event.getPriority())
                .attachments(mapEventAttachments(event.getAttachments()))
                .inlineResources(mapEventInlineResources(event.getInlineResources()))
                .build();
    }

    /**
     * Map EmailAttachment list to Event AttachmentData list
     */
    private List<EmailNotificationEvent.AttachmentData> mapAttachments(
            List<EmailAttachment> attachments) {
        
        if (attachments == null || attachments.isEmpty()) {
            return Collections.emptyList();
        }

        return attachments.stream()
                .map(attachment -> EmailNotificationEvent.AttachmentData.builder()
                        .filename(attachment.getFilename())
                        .content(attachment.getContent())
                        .contentType(attachment.getContentType())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Map InlineResource list to Event InlineResourceData list
     */
    private List<EmailNotificationEvent.InlineResourceData> mapInlineResources(
            List<InlineResource> inlineResources) {
        
        if (inlineResources == null || inlineResources.isEmpty()) {
            return Collections.emptyList();
        }

        return inlineResources.stream()
                .map(resource -> EmailNotificationEvent.InlineResourceData.builder()
                        .contentId(resource.getContentId())
                        .content(resource.getContent())
                        .contentType(resource.getContentType())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Map Event AttachmentData list to EmailAttachment list
     */
    private List<EmailAttachment> mapEventAttachments(
            List<EmailNotificationEvent.AttachmentData> attachments) {
        
        if (attachments == null || attachments.isEmpty()) {
            return Collections.emptyList();
        }

        return attachments.stream()
                .map(attachment -> EmailAttachment.builder()
                        .filename(attachment.getFilename())
                        .content(attachment.getContent())
                        .contentType(attachment.getContentType())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Map Event InlineResourceData list to InlineResource list
     */
    private List<InlineResource> mapEventInlineResources(
            List<EmailNotificationEvent.InlineResourceData> inlineResources) {
        
        if (inlineResources == null || inlineResources.isEmpty()) {
            return Collections.emptyList();
        }

        return inlineResources.stream()
                .map(resource -> InlineResource.builder()
                        .contentId(resource.getContentId())
                        .content(resource.getContent())
                        .contentType(resource.getContentType())
                        .build())
                .collect(Collectors.toList());
    }
}