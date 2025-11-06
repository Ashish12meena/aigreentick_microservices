// src/main/java/com/aigreentick/services/notification/repository/OutboxEventRepository.java
package com.aigreentick.services.notification.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aigreentick.services.notification.enums.OutboxEventStatus;
import com.aigreentick.services.notification.model.entity.OutboxEvent;

@Repository
public interface OutboxEventRepository extends MongoRepository<OutboxEvent, String> {
    
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
    
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}