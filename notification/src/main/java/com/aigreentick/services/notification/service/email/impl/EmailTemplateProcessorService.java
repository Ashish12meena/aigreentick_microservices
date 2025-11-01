package com.aigreentick.services.notification.service.email.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.aigreentick.services.notification.dto.request.email.EmailNotificationRequest;
import com.aigreentick.services.notification.exceptions.EmailTemplateNotFoundException;
import com.aigreentick.services.notification.exceptions.EmailTemplateProcessingException;
import com.aigreentick.services.notification.model.entity.EmailTemplate;
import com.aigreentick.services.notification.repository.EmailTemplateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateProcessorService {

    private final EmailTemplateRepository templateRepository;

    @Qualifier("stringTemplateEngine")
    private final TemplateEngine stringTemplateEngine;

    @Qualifier("fileTemplateEngine")
    private final TemplateEngine fileTemplateEngine;

    public EmailNotificationRequest processTemplate(
            String templateId,
            Map<String, Object> variables,
            EmailNotificationRequest baseRequest) {

        log.info("Processing template: {} with {} variables",
                templateId, variables.size());

        try {
            EmailTemplate template = templateRepository.findById(templateId)
                    .orElseThrow(() -> new EmailTemplateNotFoundException(
                            "Template not found: " + templateId));

            if (!template.isActive()) {
                throw new EmailTemplateProcessingException(
                        "Template is inactive: " + templateId, null);
            }

            Context context = new Context();
            context.setVariables(variables);

            String processedSubject = stringTemplateEngine.process(
                    template.getSubject(), context);

            String processedBody = stringTemplateEngine.process(
                    template.getBody(), context);

            return baseRequest.toBuilder()
                    .subject(processedSubject)
                    .body(processedBody)
                    .isHtml(true)
                    .build();

        } catch (EmailTemplateNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing template: {}", templateId, e);
            throw new EmailTemplateProcessingException(
                    "Failed to process template: " + templateId, e);
        }
    }

    public EmailNotificationRequest processTemplateByCode(
            String templateCode,
            Map<String, Object> variables,
            EmailNotificationRequest baseRequest) {

        log.info("Processing template by code: {}", templateCode);

        EmailTemplate template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new EmailTemplateNotFoundException(
                        "Template not found: " + templateCode));

        return processTemplate(template.getId(), variables, baseRequest);
    }

    public String processFileTemplate(String templateName, Map<String, Object> variables) {
        log.info("Processing file template: {}", templateName);

        Context context = new Context();
        context.setVariables(variables);

        return fileTemplateEngine.process(templateName, context);
    }

    public boolean validateTemplate(String templateContent) {
        try {
            Context context = new Context();
            stringTemplateEngine.process(templateContent, context);
            return true;
        } catch (Exception e) {
            log.error("Template validation failed", e);
            return false;
        }
    }
}
