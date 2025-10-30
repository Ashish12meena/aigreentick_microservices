package com.aigreentick.services.notification.provider.email;

import com.aigreentick.services.notification.dto.request.email.EmailNotificationRequest;
import com.aigreentick.services.notification.enums.NotificationChannel;

public interface EmailProviderStrategy {
    void send(EmailNotificationRequest request);

    String getProviderType(); 

    boolean isAvailable();

    int getPriority();

    default NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}
