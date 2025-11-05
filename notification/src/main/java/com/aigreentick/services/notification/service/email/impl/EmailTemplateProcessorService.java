package com.aigreentick.services.notification.service.email.impl;

import java.time.Duration;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final TemplateEngine stringTemplateEngine;
    private final RedisTemplate<String, EmailTemplate> redisTemplate;
    
    private static final String TEMPLATE_CACHE_KEY = "email:template:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    @Cacheable(value = "emailTemplates", key = "#templateCode", unless = "#result == null")
    public EmailTemplate getTemplateByCode(String templateCode) {
        log.debug("Fetching template from database: {}", templateCode);
        
        return templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new EmailTemplateNotFoundException(
                        "Template not found: " + templateCode));
    }

    public EmailNotificationRequest processTemplateByCode(
            String templateCode,
            Map<String, Object> variables,
            EmailNotificationRequest baseRequest) {
        
        log.info("Processing template by code: {}", templateCode);

        // Try cache first
        EmailTemplate template = getTemplateByCode(templateCode);

        if (!template.isActive()) {
            throw new EmailTemplateProcessingException(
                    "Template is inactive: " + templateCode);
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
    }

    @CacheEvict(value = "emailTemplates", key = "#templateCode")
    public void evictTemplateCache(String templateCode) {
        log.info("Evicting template cache for: {}", templateCode);
    }

}