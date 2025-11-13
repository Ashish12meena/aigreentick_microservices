package com.aigreentick.services.notification.provider.push;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.aigreentick.services.notification.config.properties.PushProperties;
import com.aigreentick.services.notification.dto.request.push.PushNotificationRequest;
import com.aigreentick.services.notification.enums.push.PushProviderType;
import com.aigreentick.services.notification.exceptions.PushNotificationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Web Push Provider
 * Handles browser-based push notifications using Web Push Protocol
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "push.web", name = "enabled", havingValue = "true")
public class WebPushProvider implements PushProviderStrategy {

    private final PushProperties pushProperties;

    @Override
    public void send(PushNotificationRequest request) {
        log.info("Sending Web Push notification to token: {}...",
                request.getDeviceToken().substring(0, 10));

        try {
            validateWebPushConfiguration();
            
            Map<String, Object> payload = buildWebPushPayload(request);
            
            log.info("Web Push payload built successfully: {}", payload);
            
            log.info("Successfully sent Web Push notification");

        } catch (Exception e) {
            log.error("Failed to send Web Push notification: {}", e.getMessage(), e);
            throw new PushNotificationException("Failed to send Web Push notification", e);
        }
    }

    private void validateWebPushConfiguration() {
        PushProperties.WebConfig config = pushProperties.getWeb();
        
        if (config.getVapidPublicKey() == null || config.getVapidPublicKey().isBlank()) {
            throw new PushNotificationException("Web Push VAPID public key not configured");
        }
        
        if (config.getVapidPrivateKey() == null || config.getVapidPrivateKey().isBlank()) {
            throw new PushNotificationException("Web Push VAPID private key not configured");
        }
        
        if (config.getSubject() == null || config.getSubject().isBlank()) {
            throw new PushNotificationException("Web Push subject not configured");
        }
    }

    private Map<String, Object> buildWebPushPayload(PushNotificationRequest request) {
        Map<String, Object> payload = new HashMap<>();
        
        // Notification object
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", request.getTitle());
        notification.put("body", request.getBody());
        
        // Icon and badge
        if (request.getImageUrl() != null) {
            notification.put("icon", request.getImageUrl());
            notification.put("image", request.getImageUrl());
        }
        
        // Badge count
        if (request.getBadge() != null) {
            notification.put("badge", request.getBadge());
        }
        
        // Action on click
        if (request.getClickAction() != null) {
            notification.put("data", Map.of("url", request.getClickAction()));
        }
        
        // Require interaction
        notification.put("requireInteraction", false);
        
        // Vibrate pattern
        notification.put("vibrate", new int[]{200, 100, 200});
        
        payload.put("notification", notification);
        
        // Custom data
        if (request.getData() != null && !request.getData().isEmpty()) {
            payload.put("data", request.getData());
        }
        
        // TTL (Time To Live)
        if (request.getTtl() != null) {
            payload.put("ttl", request.getTtl());
        } else {
            payload.put("ttl", 86400); // 24 hours default
        }
        
        return payload;
    }

    @Override
    public PushProviderType getProviderType() {
        return PushProviderType.WEB_PUSH;
    }

    @Override
    public boolean isAvailable() {
        if (!pushProperties.getWeb().isEnabled()) {
            return false;
        }
        
        try {
            PushProperties.WebConfig config = pushProperties.getWeb();
            
            boolean hasPublicKey = config.getVapidPublicKey() != null && 
                                  !config.getVapidPublicKey().isBlank();
            boolean hasPrivateKey = config.getVapidPrivateKey() != null && 
                                   !config.getVapidPrivateKey().isBlank();
            boolean hasSubject = config.getSubject() != null && 
                                !config.getSubject().isBlank();
            
            return hasPublicKey && hasPrivateKey && hasSubject;
            
        } catch (Exception e) {
            log.error("Web Push provider health check failed", e);
            return false;
        }
    }

    @Override
    public int getPriority() {
        return pushProperties.getWeb().getPriority();
    }
}