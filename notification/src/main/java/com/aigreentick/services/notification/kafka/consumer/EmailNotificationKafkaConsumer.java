// src/main/java/com/aigreentick/services/notification/kafka/consumer/EmailNotificationKafkaConsumer.java
package com.aigreentick.services.notification.kafka.consumer;

import java.util.Random;

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
import com.aigreentick.services.notification.service.idempotency.IdempotencyService;

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
    private final IdempotencyService idempotencyService;

    private final Random random = new Random();

    @KafkaListener(topics = "#{kafkaTopicProperties.topics.emailNotification}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "emailNotificationKafkaListenerContainerFactory")
    public void consumeEmailNotification(
            @Payload EmailNotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received email notification event from Kafka. Topic: {}, Partition: {}, Offset: {}, EventId: {}",
                topic, partition, offset, event.getEventId());

        // Check idempotency first
        if (!idempotencyService.isFirstProcessing(event.getEventId(

        ))) {
            log.warn("Duplicate event detected, skipping processing. EventId: {}", event.getEventId());
            acknowledgment.acknowledge();
            return;
        }

        try {
            processEmailNotification(event);

            // Mark as successfully processed
            idempotencyService.markAsProcessed(event.getEventId(), event.getEventId());

            acknowledgment.acknowledge();
            log.info("Email notification processed successfully and acknowledged. EventId: {}",
                    event.getEventId());

        } catch (Exception e) {
            log.error("Error processing email notification. EventId: {}", event.getEventId(), e);

            // Mark as failed in idempotency store
            idempotencyService.markAsFailed(event.getEventId(), e.getMessage());

            handleProcessingFailure(event, e, acknowledgment);
        }
    }

    @KafkaListener(topics = "#{kafkaTopicProperties.topics.emailNotificationRetry}", groupId = "${spring.kafka.consumer.group-id}-retry", containerFactory = "retryKafkaListenerContainerFactory")
    public void consumeRetryEmailNotification(
            @Payload EmailNotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        log.info("Received email notification from retry topic. EventId: {}, RetryCount: {}",
                event.getEventId(), event.getRetryCount());

        // Check idempotency
        if (!idempotencyService.isFirstProcessing(event.getEventId())) {
            log.warn("Duplicate retry event detected, skipping. EventId: {}", event.getEventId());
            acknowledgment.acknowledge();
            return;
        }

        try {
            Integer retryCount = event.getRetryCount() != null ? event.getRetryCount() : 0;

            if (retryCount >= topicProperties.getRetry().getMaxAttempts()) {
                log.error("Max retry attempts reached for event. Sending to DLQ. EventId: {}",
                        event.getEventId());
                kafkaProducerService.sendToDlq(event, "Max retry attempts exceeded");
                idempotencyService.markAsFailed(event.getEventId(), "Max retries exceeded");
                acknowledgment.acknowledge();
                return;
            }

            // Calculate backoff with jitter
            long backoffMs = calculateBackoffWithJitter(retryCount);
            log.info("Waiting {}ms before retry attempt {}. EventId: {}",
                    backoffMs, retryCount + 1, event.getEventId());

            // Use CompletableFuture for non-blocking delay
            java.util.concurrent.CompletableFuture.delayedExecutor(
                    backoffMs,
                    java.util.concurrent.TimeUnit.MILLISECONDS)
                    .execute(() -> {
                        try {
                            processEmailNotification(event);
                            idempotencyService.markAsProcessed(event.getEventId(), event.getEventId());
                            acknowledgment.acknowledge();
                            log.info("Retry email notification processed successfully. EventId: {}",
                                    event.getEventId());
                        } catch (Exception e) {
                            log.error("Error processing retry email notification. EventId: {}",
                                    event.getEventId(), e);
                            idempotencyService.markAsFailed(event.getEventId(), e.getMessage());
                            handleRetryFailure(event, e, acknowledgment);
                        }
                    });

        } catch (Exception e) {
            log.error("Error in retry consumer. EventId: {}", event.getEventId(), e);
            idempotencyService.markAsFailed(event.getEventId(), e.getMessage());
            acknowledgment.acknowledge();
        }
    }

    private void processEmailNotification(EmailNotificationEvent event) {
        log.debug("Processing email notification event. EventId: {}", event.getEventId());

        EmailNotificationRequest request;
        if (event.getTemplateCode() != null && !event.getTemplateCode().isEmpty()) {
            EmailNotificationRequest baseRequest = emailEventMapper.toEmailRequest(event);
            request = templateProcessor.processTemplateByCode(
                    event.getTemplateCode(),
                    event.getTemplateVariables(),
                    baseRequest);
        } else {
            request = emailEventMapper.toEmailRequest(event);
        }

        long startTime = System.currentTimeMillis();
        EmailNotification notification = emailDeliveryService.deliver(request, event.getEventId());
        long processingTime = System.currentTimeMillis() - startTime;

        log.info("Email delivered successfully. EventId: {}, NotificationId: {}, ProcessingTime: {}ms",
                event.getEventId(), notification.getId(), processingTime);

        kafkaProducerService.sendSuccessEvent(event, notification.getId());
    }

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
     * Calculate exponential backoff with jitter to prevent thundering herd
     */
    private long calculateBackoffWithJitter(int retryCount) {
        long backoffMs = topicProperties.getRetry().getBackoffMs();
        double multiplier = topicProperties.getRetry().getMultiplier();
        long maxBackoffMs = topicProperties.getRetry().getMaxBackoffMs();

        // Exponential backoff
        long delay = (long) (backoffMs * Math.pow(multiplier, retryCount));
        delay = Math.min(delay, maxBackoffMs);

        // Add jitter: randomize ±25% of the delay
        long jitter = (long) (delay * 0.25 * (random.nextDouble() - 0.5) * 2);
        delay += jitter;

        return Math.max(delay, backoffMs); // Ensure minimum delay
    }
}