package com.aigreentick.services.notification.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.aigreentick.services.notification.config.properties.KafkaTopicProperties;
import com.aigreentick.services.notification.dto.request.email.EmailNotificationRequest;
import com.aigreentick.services.notification.kafka.event.EmailNotificationEvent;
import com.aigreentick.services.notification.kafka.producer.KafkaProducerService;
import com.aigreentick.services.notification.mapper.EmailEventMapper;
import com.aigreentick.services.notification.model.entity.EmailNotification;
import com.aigreentick.services.notification.service.email.impl.EmailDeliveryServiceImpl;
import com.aigreentick.services.notification.service.email.impl.EmailTemplateProcessorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationKafkaConsumer {

    private final EmailDeliveryServiceImpl emailDeliveryService;
    private final EmailTemplateProcessorService templateProcessor;
    private final KafkaProducerService kafkaProducerService;
    private final KafkaTopicProperties topicProperties;
    private final EmailEventMapper emailEventMapper;

    /**
     * Main consumer for email notification events
     */
    @KafkaListener(
            topics = "#{kafkaTopicProperties.topics.emailNotification}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "emailNotificationKafkaListenerContainerFactory"
    )
    public void consumeEmailNotification(
            @Payload EmailNotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received email notification event from Kafka. Topic: {}, Partition: {}, Offset: {}, EventId: {}",
                topic, partition, offset, event.getEventId());

        try {
            processEmailNotification(event);
            
            acknowledgment.acknowledge();
            log.info("Email notification processed successfully and acknowledged. EventId: {}", 
                    event.getEventId());
            
        } catch (Exception e) {
            log.error("Error processing email notification. EventId: {}", event.getEventId(), e);
            handleProcessingFailure(event, e, acknowledgment);
        }
    }

    /**
     * Consumer for retry topic
     */
    @KafkaListener(
            topics = "#{kafkaTopicProperties.topics.emailNotificationRetry}",
            groupId = "${spring.kafka.consumer.group-id}-retry",
            containerFactory = "retryKafkaListenerContainerFactory"
    )
    public void consumeRetryEmailNotification(
            @Payload EmailNotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        log.info("Received email notification from retry topic. EventId: {}, RetryCount: {}",
                event.getEventId(), event.getRetryCount());

        try {
            Integer retryCount = event.getRetryCount() != null ? event.getRetryCount() : 0;
            
            if (retryCount >= topicProperties.getRetry().getMaxAttempts()) {
                log.error("Max retry attempts reached for event. Sending to DLQ. EventId: {}", 
                        event.getEventId());
                kafkaProducerService.sendToDlq(event, "Max retry attempts exceeded");
                acknowledgment.acknowledge();
                return;
            }

            long backoffMs = calculateBackoff(retryCount);
            log.info("Waiting {}ms before retry attempt {}. EventId: {}", 
                    backoffMs, retryCount + 1, event.getEventId());
            
            Thread.sleep(backoffMs);
            
            processEmailNotification(event);
            
            acknowledgment.acknowledge();
            log.info("Retry email notification processed successfully. EventId: {}", 
                    event.getEventId());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Retry processing interrupted. EventId: {}", event.getEventId(), e);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing retry email notification. EventId: {}", 
                    event.getEventId(), e);
            handleRetryFailure(event, e, acknowledgment);
        }
    }

    /**
     * Process the email notification event
     */
    private void processEmailNotification(EmailNotificationEvent event) {
        log.debug("Processing email notification event. EventId: {}", event.getEventId());
        
        // Check if it's a template-based notification
        EmailNotificationRequest request;
        if (event.getTemplateCode() != null && !event.getTemplateCode().isEmpty()) {
            // Process template first
            EmailNotificationRequest baseRequest = emailEventMapper.toEmailRequest(event);
            request = templateProcessor.processTemplateByCode(
                    event.getTemplateCode(),
                    event.getTemplateVariables(),
                    baseRequest);
        } else {
            // Direct email content
            request = emailEventMapper.toEmailRequest(event);
        }
        
        long startTime = System.currentTimeMillis();
        EmailNotification notification = emailDeliveryService.deliver(request, event.getEventId());
        long processingTime = System.currentTimeMillis() - startTime;
        
        log.info("Email delivered successfully. EventId: {}, NotificationId: {}, ProcessingTime: {}ms",
                event.getEventId(), notification.getId(), processingTime);
        
        // Send success event
        kafkaProducerService.sendSuccessEvent(event, notification.getId());
    }

    /**
     * Handle processing failure for main topic
     */
    private void handleProcessingFailure(
            EmailNotificationEvent event, 
            Exception exception, 
            Acknowledgment acknowledgment) {
        
        try {
            Integer retryCount = event.getRetryCount() != null ? event.getRetryCount() : 0;
            
            if (retryCount < topicProperties.getRetry().getMaxAttempts()) {
                event.setRetryCount(retryCount + 1);
                kafkaProducerService.sendToRetryTopic(event);
                log.info("Email notification sent to retry topic. EventId: {}, RetryCount: {}",
                        event.getEventId(), event.getRetryCount());
            } else {
                kafkaProducerService.sendToDlq(event, exception.getMessage());
                kafkaProducerService.sendFailedEvent(event, exception.getMessage());
                log.error("Max retry attempts reached. Sent to DLQ. EventId: {}", 
                        event.getEventId());
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Critical error handling processing failure. EventId: {}", 
                    event.getEventId(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Handle retry processing failure
     */
    private void handleRetryFailure(
            EmailNotificationEvent event, 
            Exception exception, 
            Acknowledgment acknowledgment) {
        
        try {
            Integer retryCount = event.getRetryCount() != null ? event.getRetryCount() : 0;
            
            if (retryCount < topicProperties.getRetry().getMaxAttempts()) {
                event.setRetryCount(retryCount + 1);
                kafkaProducerService.sendToRetryTopic(event);
            } else {
                kafkaProducerService.sendToDlq(event, exception.getMessage());
                kafkaProducerService.sendFailedEvent(event, exception.getMessage());
            }
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Critical error handling retry failure. EventId: {}", 
                    event.getEventId(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Calculate exponential backoff delay
     */
    private long calculateBackoff(int retryCount) {
        long backoffMs = topicProperties.getRetry().getBackoffMs();
        double multiplier = topicProperties.getRetry().getMultiplier();
        long maxBackoffMs = topicProperties.getRetry().getMaxBackoffMs();
        
        long delay = (long) (backoffMs * Math.pow(multiplier, retryCount));
        return Math.min(delay, maxBackoffMs);
    }
}