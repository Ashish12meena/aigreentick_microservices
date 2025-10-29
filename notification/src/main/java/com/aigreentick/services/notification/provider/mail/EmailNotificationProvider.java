package com.aigreentick.services.notification.provider.mail;

import com.aigreentick.services.notification.dto.email.EmailNotificationRequest;
import com.aigreentick.services.notification.enums.NotificationChannel;

public interface EmailNotificationProvider {
    void send(EmailNotificationRequest request);

    String getProviderType(); 

    boolean isAvailable();

    int getPriority();

    default NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}
