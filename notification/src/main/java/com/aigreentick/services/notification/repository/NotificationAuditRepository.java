package com.aigreentick.services.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aigreentick.services.notification.model.entity.NotificationAudit;

@Repository
public interface NotificationAuditRepository extends MongoRepository<NotificationAudit, String> {
    
    Page<NotificationAudit> findByNotificationId(String notificationId, Pageable pageable);
    
    Page<NotificationAudit> findByUserId(String userId, Pageable pageable);
    
    Page<NotificationAudit> findByCorrelationId(String correlationId, Pageable pageable);
    
    Page<NotificationAudit> findByEventId(String eventId, Pageable pageable);
}
