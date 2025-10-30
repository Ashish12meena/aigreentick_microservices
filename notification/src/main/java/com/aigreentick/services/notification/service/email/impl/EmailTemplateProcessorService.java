package com.aigreentick.services.notification.service.email.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import com.aigreentick.services.notification.dto.request.email.EmailNotificationRequest;
import com.aigreentick.services.notification.exceptions.EmailTemplateNotFoundException;
import com.aigreentick.services.notification.exceptions.EmailTemplateProcessingException;
import com.aigreentick.services.notification.model.entity.EmailTemplate;
import com.aigreentick.services.notification.repository.EmailTemplateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for processing email templates with variable substitution
 * Uses Thymeleaf for template rendering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateProcessorService {
    
    private final EmailTemplateRepository templateRepository;
    private final TemplateEngine templateEngine;
    
    /**
     * Constructor to configure Thymeleaf for string-based templates
     */
    public EmailTemplateProcessorService(EmailTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
        this.templateEngine = createTemplateEngine();
    }
    
    /**
     * Process template and merge with notification request
     * 
     * @param templateId Template identifier
     * @param variables Variables for template substitution
     * @param baseRequest Base request to merge with template
     * @return Updated request with processed template
     */
    public EmailNotificationRequest processTemplate(
            String templateId, 
            Map<String, Object> variables,
            EmailNotificationRequest baseRequest) {
        
        log.info("Processing template: {} with {} variables", templateId, variables.size());
        
        try {
            // Fetch template from database
            EmailTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EmailTemplateNotFoundException("Template not found: " + templateId));
            
            // Create Thymeleaf context
            Context context = new Context();
            context.setVariables(variables);
            
            // Process subject
            String processedSubject = templateEngine.process(template.getSubject(), context);
            
            // Process body
            String processedBody = templateEngine.process(template.getBody(), context); 
            
            // Build updated request
            return baseRequest.toBuilder()
                .subject(processedSubject)
                .body(processedBody)
                .isHtml(true) // Templates are typically HTML
                .build();
                
        } catch (EmailTemplateNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing template: {}", templateId, e);
            throw new EmailTemplateProcessingException("Failed to process template: " + templateId, e);
        }
    }
    
    /**
     * Process template with template code (instead of ID)
     * 
     * @param templateCode Template code
     * @param variables Variables for substitution
     * @return Processed HTML content
     */
    public String processTemplateByCode(String templateCode, Map<String, Object> variables) {
        EmailTemplate template = templateRepository.findByTemplateCode(templateCode)
            .orElseThrow(() -> new EmailTemplateNotFoundException("Template not found: " + templateCode));
        
        Context context = new Context();
        context.setVariables(variables);
        
        return templateEngine.process(template.getBody(), context);
    }
    
    /**
     * Validate template syntax without processing
     * 
     * @param templateContent Template content to validate
     * @return true if valid
     */
    public boolean validateTemplate(String templateContent) {
        try {
            Context context = new Context();
            templateEngine.process(templateContent, context);
            return true;
        } catch (Exception e) {
            log.error("Template validation failed", e);
            return false;
        }
    }
    
    /**
     * Create and configure Thymeleaf template engine for string templates
     */
    private TemplateEngine createTemplateEngine() {
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false); // Disable cache for dynamic DB templates
        
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
        
        return engine;
    }
}