package com.aigreentick.services.notification.service.provider;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.aigreentick.services.notification.config.properties.EmailProperties;
import com.aigreentick.services.notification.dto.email.EmailNotificationRequest;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for sending email notifications with support for HTML templates,
 * attachments, CC/BCC recipients, and async processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotifcationProvider {
    private final JavaMailSender mailSender;
    private final EmailProperties properties;

    public void sendEmail(EmailNotificationRequest request) throws MessagingException {

        try {
            MimeMessage message = buildMimeMessage(request);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", request.getTo());
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to: {}", request.getTo(), e);
            throw new MessagingException("Email sending failed: " + e.getMessage(), e);
        }
    }

    private MimeMessage buildMimeMessage(EmailNotificationRequest request) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(properties.getFromEmail());
        helper.setTo(request.getTo().toArray(new String[0]));
        helper.setSubject(request.getSubject());
        helper.setText(request.getBody(), request.isHtml());

        // Add CC recipients
        if (!CollectionUtils.isEmpty(request.getCc())) {
            helper.setCc(request.getCc().toArray(new String[0]));
        }

        // Add BCC recipients
        if (!CollectionUtils.isEmpty(request.getBcc())) {
            helper.setBcc(request.getBcc().toArray(new String[0]));
        }

        // Add attachments
        if (!CollectionUtils.isEmpty(request.getAttachments())) {
            request.getAttachments().forEach(attachment -> {
                try {
                    helper.addAttachment(
                            attachment.getFilename(),
                            new ByteArrayResource(attachment.getContent()),
                            attachment.getContentType());
                } catch (MessagingException e) {
                    log.error("Failed to add attachment: {}", attachment.getFilename(), e);
                }
            });
        }

        // Add inline resources (for embedded images in HTML)
        if (!CollectionUtils.isEmpty(request.getInlineResources())) {
            request.getInlineResources().forEach(resource -> {
                try {
                    helper.addInline(
                            resource.getContentId(),
                            new ByteArrayResource(resource.getContent()),
                            resource.getContentType());
                } catch (MessagingException e) {
                    log.error("Failed to add inline resource: {}", resource.getContentId(), e);
                }
            });
        }

        // Set priority if specified
        if (request.getPriority() != null) {
            message.setHeader("X-Priority", String.valueOf(request.getPriority().getValue()));
        }

        return message;
    }
}
