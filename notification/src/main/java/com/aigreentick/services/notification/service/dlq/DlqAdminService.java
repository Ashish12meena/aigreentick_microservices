// src/main/java/com/aigreentick/services/notification/service/dlq/DlqAdminService.java
package com.aigreentick.services.notification.service.dlq;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aigreentick.services.notification.dto.request.DlqRetryRequest;
import com.aigreentick.services.notification.dto.response.DlqStats;
import com.aigreentick.services.notification.kafka.event.EmailNotificationEvent;
import com.aigreentick.services.notification.kafka.producer.KafkaProducerService;
import com.aigreentick.services.notification.model.entity.DlqMessage;
import com.aigreentick.services.notification.repository.DlqMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing Dead Letter Queue messages
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DlqAdminService {

    private final DlqMessageRepository dlqRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<DlqMessage> getUnprocessedMessages(Pageable pageable) {
        return dlqRepository.findByProcessedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public DlqMessage getDlqMessageById(String id) {
        return dlqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DLQ message not found: " + id));
    }

    @Transactional(readOnly = true)
    public DlqStats getDlqStats() {
        long totalUnprocessed = dlqRepository.countByProcessedFalse();
        long totalProcessed = dlqRepository.count() - totalUnprocessed;
        
        return DlqStats.builder()
                .totalUnprocessed(totalUnprocessed)
                .totalProcessed(totalProcessed)
                .total(dlqRepository.count())
                .build();
    }

    @Transactional
    public void retryDlqMessage(String id, DlqRetryRequest request) {
        DlqMessage dlqMessage = getDlqMessageById(id);
        
        try {
            // Deserialize event
            EmailNotificationEvent event = objectMapper.readValue(
                    dlqMessage.getPayload(), 
                    EmailNotificationEvent.class);
            
            // Reset retry count
            event.setRetryCount(0);
            
            // Remove DLQ metadata
            if (event.getMetadata() != null) {
                event.getMetadata().remove("dlqReason");
                event.getMetadata().remove("dlqTimestamp");
            }
            
            // Republish to main topic
            kafkaProducerService.sendEmailNotification(event);
            
            // Mark as processed
            dlqMessage.setProcessed(true);
            dlqMessage.setReprocessedBy(request.getRequestedBy());
            dlqMessage.setReprocessingNotes(request.getNotes());
            dlqMessage.setUpdatedAt(Instant.now());
            
            dlqRepository.save(dlqMessage);
            
            log.info("DLQ message retried successfully: {}", id);
            
        } catch (Exception e) {
            log.error("Error retrying DLQ message: {}", id, e);
            throw new RuntimeException("Failed to retry DLQ message", e);
        }
    }

    @Transactional
    public int retryUnprocessedMessages(DlqRetryRequest request) {
        List<DlqMessage> unprocessedMessages = dlqRepository.findByProcessedFalseOrderByCreatedAtAsc();
        
        int retried = 0;
        for (DlqMessage dlqMessage : unprocessedMessages) {
            try {
                retryDlqMessage(dlqMessage.getId(), request);
                retried++;
            } catch (Exception e) {
                log.error("Error retrying DLQ message in batch: {}", dlqMessage.getId(), e);
            }
        }
        
        log.info("Batch retry completed. Retried {} out of {} messages", 
                retried, unprocessedMessages.size());
        
        return retried;
    }

    @Transactional
    public void markAsProcessed(String id, String requestedBy, String notes) {
        DlqMessage dlqMessage = getDlqMessageById(id);
        
        dlqMessage.setProcessed(true);
        dlqMessage.setReprocessedBy(requestedBy);
        dlqMessage.setReprocessingNotes(notes != null ? notes : "Manually marked as processed");
        dlqMessage.setUpdatedAt(Instant.now());
        
        dlqRepository.save(dlqMessage);
        
        log.info("DLQ message marked as processed: {}", id);
    }
}