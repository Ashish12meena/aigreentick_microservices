package com.aigreentick.services.notification.provider.push;

import com.aigreentick.services.notification.enums.NotificationChannel;

/**
 * Specific interface for Push notification providers (FCM, APNs)
 * Extends the generic NotificationProvider with Push-specific type
 */
public interface PushNotificationProvider  {
    
    default NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }
    
}

