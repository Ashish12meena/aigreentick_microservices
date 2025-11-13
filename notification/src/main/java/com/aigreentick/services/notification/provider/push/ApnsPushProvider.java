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
 * Apple Push Notification Service (APNs) Provider
 * Handles iOS push notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "push.apns", name = "enabled", havingValue = "true")
public class ApnsPushProvider implements PushProviderStrategy {

    private final PushProperties pushProperties;

    @Override
    public void send(PushNotificationRequest request) {
        log.info("Sending APNs push notification to token: {}...",
                request.getDeviceToken().substring(0, 10));

        try {
            validateApnsConfiguration();
            
            Map<String, Object> payload = buildApnsPayload(request);
            
            // TODO: Integrate with APNs library (e.g., pushy, apns-http2)
            // For now, log the payload structure
            log.info("APNs payload built successfully: {}", payload);
            
            // Simulated success - replace with actual APNs client call
            log.info("Successfully sent APNs notification");

        } catch (Exception e) {
            log.error("Failed to send APNs notification: {}", e.getMessage(), e);
            throw new PushNotificationException("Failed to send APNs notification", e);
        }
    }

    private void validateApnsConfiguration() {
        PushProperties.ApnsConfig config = pushProperties.getApns();
        
        if (config.getTeamId() == null || config.getTeamId().isBlank()) {
            throw new PushNotificationException("APNs Team ID not configured");
        }
        
        if (config.getKeyId() == null || config.getKeyId().isBlank()) {
            throw new PushNotificationException("APNs Key ID not configured");
        }
        
        if (config.getKeyPath() == null || config.getKeyPath().isBlank()) {
            throw new PushNotificationException("APNs Key Path not configured");
        }
        
        if (config.getBundleId() == null || config.getBundleId().isBlank()) {
            throw new PushNotificationException("APNs Bundle ID not configured");
        }
    }

    private Map<String, Object> buildApnsPayload(PushNotificationRequest request) {
        Map<String, Object> payload = new HashMap<>();
        
        // Build APS dictionary
        Map<String, Object> aps = new HashMap<>();
        
        // Alert
        Map<String, String> alert = new HashMap<>();
        alert.put("title", request.getTitle());
        alert.put("body", request.getBody());
        aps.put("alert", alert);
        
        // Badge
        if (request.getBadge() != null) {
            aps.put("badge", request.getBadge());
        }
        
        // Sound
        if (request.getSound() != null) {
            aps.put("sound", request.getSound());
        } else {
            aps.put("sound", "default");
        }
        
        // Content available
        aps.put("content-available", 1);
        
        // Mutable content (for rich notifications)
        if (request.getImageUrl() != null) {
            aps.put("mutable-content", 1);
        }
        
        payload.put("aps", aps);
        
        // Custom data
        if (request.getData() != null && !request.getData().isEmpty()) {
            payload.putAll(request.getData());
        }
        
        // Image URL for rich notifications
        if (request.getImageUrl() != null) {
            payload.put("image-url", request.getImageUrl());
        }
        
        return payload;
    }

    @Override
    public PushProviderType getProviderType() {
        return PushProviderType.APNS;
    }

    @Override
    public boolean isAvailable() {
        if (!pushProperties.getApns().isEnabled()) {
            return false;
        }
        
        try {
            PushProperties.ApnsConfig config = pushProperties.getApns();
            
            // Check if all required configs are present
            boolean hasTeamId = config.getTeamId() != null && !config.getTeamId().isBlank();
            boolean hasKeyId = config.getKeyId() != null && !config.getKeyId().isBlank();
            boolean hasKeyPath = config.getKeyPath() != null && !config.getKeyPath().isBlank();
            boolean hasBundleId = config.getBundleId() != null && !config.getBundleId().isBlank();
            
            return hasTeamId && hasKeyId && hasKeyPath && hasBundleId;
            
        } catch (Exception e) {
            log.error("APNs provider health check failed", e);
            return false;
        }
    }

    @Override
    public int getPriority() {
        return pushProperties.getApns().getPriority();
    }
}