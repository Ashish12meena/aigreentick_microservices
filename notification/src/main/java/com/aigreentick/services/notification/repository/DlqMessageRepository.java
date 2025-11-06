package com.aigreentick.services.notification.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aigreentick.services.notification.model.entity.DlqMessage;

@Repository
public interface DlqMessageRepository extends MongoRepository<DlqMessage, String> {
    
    List<DlqMessage> findByProcessedFalseOrderByCreatedAtAsc();
    
    Page<DlqMessage> findByProcessedFalse(Pageable pageable);
    
    long countByProcessedFalse();
}