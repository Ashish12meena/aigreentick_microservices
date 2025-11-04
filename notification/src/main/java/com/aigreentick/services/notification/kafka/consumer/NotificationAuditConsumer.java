package com.aigreentick.services.notification.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.aigreentick.services.notification.kafka.event.NotificationAuditEvent;
import com.aigreentick.services.notification.service.audit.NotificationAuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationAuditConsumer {

    private final NotificationAuditService auditService;

    @KafkaListener(
            topics = "#{kafkaTopicProperties.topics.notificationAudit}",
            groupId = "${spring.kafka.consumer.group-id}-audit",
            containerFactory = "emailNotificationKafkaListenerContainerFactory"
    )
    public void consumeAuditEvent(
            @Payload NotificationAuditEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received audit event from Kafka. Topic: {}, Partition: {}, Offset: {}, AuditId: {}",
                topic, partition, offset, event.getAuditId());

        try {
            auditService.saveAuditEvent(event);
            
            acknowledgment.acknowledge();
            log.debug("Audit event processed and acknowledged. AuditId: {}", event.getAuditId());
            
        } catch (Exception e) {
            log.error("Error processing audit event. AuditId: {}", event.getAuditId(), e);
            acknowledgment.acknowledge();
        }
    }
}