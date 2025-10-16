package com.example.aigreentick.services.messaging.message.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.aigreentick.services.common.dto.response.FacebookApiResponse;
import com.example.aigreentick.services.messaging.broadcast.client.dto.AccessTokenCredentials;
import com.example.aigreentick.services.messaging.broadcast.client.service.impl.UserClientImpl;
import com.example.aigreentick.services.messaging.broadcast.constants.BroadcastConstants;
import com.example.aigreentick.services.messaging.broadcast.dto.request.BroadcastRequestDTO;
import com.example.aigreentick.services.messaging.broadcast.enums.BroadcastStatus;
import com.example.aigreentick.services.messaging.broadcast.service.impl.BroadcastServiceImpl;
import com.example.aigreentick.services.messaging.message.client.service.impl.WhatsappClient;
import com.example.aigreentick.services.messaging.message.dto.build.template.Template;
import com.example.aigreentick.services.messaging.message.dto.request.MessageRequest;
import com.example.aigreentick.services.messaging.message.dto.response.ChatResponseDto;
import com.example.aigreentick.services.messaging.message.dto.response.SendTemplateMessageResponse;
import com.example.aigreentick.services.messaging.message.enums.MessageStatus;
import com.example.aigreentick.services.messaging.message.mapper.MessageMapper;
import com.example.aigreentick.services.messaging.message.model.Messages;
import com.example.aigreentick.services.messaging.message.model.content.Content;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MessagesOrchestratorServiceImpl {
    private final MessageMapper messageMapper;
    private final MessagesServiceImpl messagesServiceImpl;
    private final ObjectMapper objectMapper;
    private final ExecutorService broadcastExecutor;
    private final ExecutorService whatsappExecutor;
    private final Semaphore semaphore;
    private final BroadcastServiceImpl broadcastService;
    private final WhatsappClient whatsappMessagingServiceImpl;
    private final MongoTemplate mongoTemplate;
    private final UserClientImpl userService;

    public MessagesOrchestratorServiceImpl(
            ObjectMapper objectMapper,
            MessageMapper messageMapper,
            BroadcastServiceImpl broadcastService,
            @Qualifier("broadcastExecutor") ExecutorService broadcastExecutor,
            @Qualifier("whatsappExecutor") ExecutorService whatsappExecutor,
            Semaphore whatsappConcurrencySemaphore,
            WhatsappClient whatsappMessagingServiceImpl,
            MessagesServiceImpl messagesServiceImpl,
            MongoTemplate mongoTemplate,
            UserClientImpl userService) {
        this.objectMapper = objectMapper;
        this.broadcastExecutor = broadcastExecutor;
        this.whatsappExecutor = whatsappExecutor;
        this.semaphore = whatsappConcurrencySemaphore;
        this.broadcastService = broadcastService;
        this.whatsappMessagingServiceImpl = whatsappMessagingServiceImpl;
        this.messageMapper = messageMapper;
        this.messagesServiceImpl = messagesServiceImpl;
        this.mongoTemplate = mongoTemplate;
        this.userService = userService;
    }

    public ChatResponseDto sendMessage(MessageRequest messageRequest, Long userId) {

        AccessTokenCredentials accessAppCredentials = userService.getPhoneNumberIdAccessToken(userId);

        String bodyString = null;
        try {
            bodyString = objectMapper
                    .setSerializationInclusion(Include.NON_NULL)
                    .writeValueAsString(messageRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        FacebookApiResponse<SendTemplateMessageResponse> response = whatsappMessagingServiceImpl.sendMessage(bodyString,
                accessAppCredentials.getId(), accessAppCredentials.getAccessToken());
        if (!response.isSuccess()) {
            throw new RuntimeException();
        }

        Messages messages = messageMapper.toEntity(messageRequest, response.getData(), bodyString, accessAppCredentials.getId());

        messagesServiceImpl.save(messages);

        return messageMapper.toChatResponseDto(messages);
    }

    public void bulkUpdateMessages(List<Messages> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Messages.class);

        for (Messages r : messages) {
            Query query = new Query(Criteria.where("_id").is(r.getId()));
            ops.replaceOne(query, r); // replaces the entire document with new one
        }

        ops.execute();
    }

    public List<Messages> buildPendingMessageReports(BroadcastRequestDTO dto, List<String> filteredMobileNumbers,
            Long campaignId, Long broadcastId, Long userId, String phoneNumberId) {
        return filteredMobileNumbers.stream().map(mobile -> {
            Messages message = new Messages();
            message.setCampaignId(campaignId);
            message.setBroadcastId(broadcastId);
            message.setUserId(userId);
            message.setTo(mobile);
            message.setFrom(phoneNumberId);
            message.setStatus(MessageStatus.PENDING);
            message.setCreatedAt(LocalDateTime.now());
            message.setUpdatedAt(LocalDateTime.now());
            return message;
        }).collect(Collectors.toList());
    }

    public void saveAndProcessMessagesReports(List<List<Messages>> chunkedMessages, Template template,
            String phoneNumberId,
            String accessToken, List<MessageRequest> baseSendTemplate) {
        broadcastExecutor.submit(() -> {
            try {
                long begin = System.currentTimeMillis();
                messagesServiceImpl.saveMessagesChunks(chunkedMessages);

                log.info("Total time {} taken to save {} chunks of reports",
                        (System.currentTimeMillis() - begin), chunkedMessages.size());

                for (List<Messages> processChunk : chunkedMessages) {
                    sendAndUpdateMessagesInControlledBatches(processChunk, template, phoneNumberId,
                            accessToken, baseSendTemplate);
                }

                broadcastService.updateStatusById(BroadcastStatus.COMPLETED,
                        chunkedMessages.get(0).get(0).getBroadcastId());

                log.info("Finished processing all report chunks time taken {} ", (System.currentTimeMillis() - begin));

            } catch (Exception e) {
                log.error("Error occurred during saving or processing reports", e);
            }
        });
    }

    private void sendAndUpdateMessagesInControlledBatches(List<Messages> messages, Template template,
            String phoneNumberId, String accessToken, List<MessageRequest> baseSendTemplates) {

        // Pre-map templates by phone number for quick lookup
        Map<String, MessageRequest> templateMap = baseSendTemplates.stream()
                .collect(Collectors.toMap(MessageRequest::getTo, t -> t));

        final Queue<Messages> buffer = new ConcurrentLinkedQueue<>();
        final Object lock = new Object();
        final CountDownLatch latch = new CountDownLatch(messages.size());
        for (Messages message : messages) {
            whatsappExecutor.execute(() -> {
                try {
                    semaphore.acquire();
                    // find matching template for this report’s number
                    MessageRequest matchingTemplate = templateMap.get(message.getTo());
                    if (matchingTemplate != null) {
                        Messages updatedReport = processMessages(message, matchingTemplate, phoneNumberId, accessToken);
                        buffer.add(updatedReport);
                    } else {
                        log.warn("No matching template found for report {} with phone {}",
                                message.getId(), message.getTo());
                    }

                    // Batch flush logic
                    synchronized (lock) {
                        if (buffer.size() >= BroadcastConstants.MAX_CONCURRENT_WHATSAPP_REQUESTS) {
                            List<Messages> batch = new ArrayList<>();
                            for (int i = 0; i < BroadcastConstants.MAX_CONCURRENT_WHATSAPP_REQUESTS; i++) {
                                Messages r = buffer.poll();
                                if (r != null) {
                                    batch.add(r);
                                }
                            }
                            if (!batch.isEmpty()) {
                                bulkUpdateMessages(batch);
                                log.info("Batch of {} reports updated", batch.size());
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted while processing report {}", message.getId(), e);
                } finally {
                    semaphore.release();
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(); // wait for all tasks to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Latch await interrupted", e);
        }

        // Final flush for leftover reports
        if (!buffer.isEmpty()) {
            List<Messages> finalBatch = new ArrayList<>();
            while (!buffer.isEmpty()) {
                finalBatch.add(buffer.poll());
            }
            bulkUpdateMessages(finalBatch);
            log.info("Final batch of {} reports updated", finalBatch.size());
        }

        log.info("All reports processed and updated.");
    }

    private Messages processMessages(Messages message, MessageRequest sendTemplateBody, String phoneNumberId,
            String accessToken) {
        try {
            String sendTemplateBodyAsString = objectMapper
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writeValueAsString(sendTemplateBody);
            FacebookApiResponse<SendTemplateMessageResponse> response = whatsappMessagingServiceImpl.sendMessage(
                    sendTemplateBodyAsString,
                    phoneNumberId, accessToken);
            message.setPayload(sendTemplateBodyAsString);
            message.setContent(new Content(sendTemplateBody.getTemplate()));
            message.setResponse(objectMapper.writeValueAsString(response));

            if (response.isSuccess() && response.getData() != null) {
                SendTemplateMessageResponse responseDto = response.getData();
                if (responseDto.getContacts() != null && !responseDto.getContacts().isEmpty()) {
                    message.setWaId(responseDto.getContacts().get(0).getWaId());
                }

                if (responseDto.getMessages() != null && !responseDto.getMessages().isEmpty()) {
                    var msg = responseDto.getMessages().get(0);
                    message.setMessageId(msg.getId());
                    message.setStatus(MessageStatus.fromValue(
                            msg.getMessageStatus() != null ? msg.getMessageStatus() : "accepted"));
                }
            } else {
                message.setStatus(MessageStatus.FAILED);
                log.error("Send failed: {}", response.getErrorMessage());
            }

        } catch (Exception e) {
            message.setStatus(MessageStatus.FAILED);
            log.error("Failed to send WhatsApp message to {}", message.getTo(), e);
        }
        message.setUpdatedAt(LocalDateTime.now());
        return message;
    }
}
