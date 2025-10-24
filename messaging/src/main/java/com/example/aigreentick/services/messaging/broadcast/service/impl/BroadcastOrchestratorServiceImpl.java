package com.example.aigreentick.services.messaging.broadcast.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.aigreentick.services.common.dto.response.ResponseMessage;
import com.example.aigreentick.services.messaging.broadcast.client.dto.AccessTokenCredentials;
import com.example.aigreentick.services.messaging.broadcast.client.dto.CountryDto;
import com.example.aigreentick.services.messaging.broadcast.client.service.impl.CountryClientImpl;
import com.example.aigreentick.services.messaging.broadcast.client.service.impl.TemplateClientImpl;
import com.example.aigreentick.services.messaging.broadcast.client.service.impl.UserClientImpl;
import com.example.aigreentick.services.messaging.broadcast.constants.BroadcastConstants;
import com.example.aigreentick.services.messaging.broadcast.dto.request.BroadcastRequestDTO;
import com.example.aigreentick.services.messaging.broadcast.enums.BroadcastStatus;
import com.example.aigreentick.services.messaging.broadcast.model.Broadcast;
import com.example.aigreentick.services.messaging.message.dto.build.template.Template;
import com.example.aigreentick.services.messaging.message.dto.request.MessageRequest;
import com.example.aigreentick.services.messaging.message.model.Messages;
import com.example.aigreentick.services.messaging.message.service.impl.MessageTemplateBuilderServiceImpl;
import com.example.aigreentick.services.messaging.message.service.impl.MessagesOrchestratorServiceImpl;
import com.example.aigreentick.services.messaging.message.util.MessagingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastOrchestratorServiceImpl {
        private final UserClientImpl userService;
        private final BroadcastServiceImpl broadcastService;
        private final TemplateClientImpl templateService;
        private final CountryClientImpl countryService;
        private final MessagesOrchestratorServiceImpl messagesOrchestratorService;
        private final MessageTemplateBuilderServiceImpl templateBuilderService;

        /**
         * Dispatches a broadcast: fetches template, country, and access credentials
         * asynchronously,
         * filters numbers, creates broadcast, and sends messages in controlled batches.
         */
        public ResponseMessage<String> dispatch(BroadcastRequestDTO dto, Long userId, Long organisationId,
                        Long campaignId) {

                // Fetch required data asynchronously
                CompletableFuture<Template> templateFuture = CompletableFuture
                                .supplyAsync(() -> templateService.findById(dto.getTemlateId()));
                CompletableFuture<CountryDto> countryFuture = CompletableFuture
                                .supplyAsync(() -> countryService.findById(dto.getCountryId()));
                CompletableFuture<AccessTokenCredentials> accessCredentialsFuture = CompletableFuture
                                .supplyAsync(() -> userService.getPhoneNumberIdAccessToken(userId));

                Template template = templateFuture.join();
                CountryDto country = countryFuture.join();
                AccessTokenCredentials accessTokenCredentials = accessCredentialsFuture.join();

                // Filter mobile numbers valid for this template
                List<String> filteredMobileNumbers = broadcastService.filterNumberForTemplateMessage(country,
                                dto.getMobileNumbers(), userId);

                // Create broadcast entry in DB
                Broadcast broadcast = broadcastService.createAndSaveBroadcast(dto, filteredMobileNumbers, userId,
                                country,
                                template, organisationId);

                // Handle scheduled broadcasts
                if (isScheduledBroadcast(dto, broadcast)) {
                        return scheduledResponse(dto.getScheduledAt());
                }

                // Build pending message reports
                List<Messages> messages = messagesOrchestratorService.buildPendingMessageReports(dto,
                                filteredMobileNumbers,
                                campaignId, broadcast.getId(), userId, accessTokenCredentials.getId());

                // Split messages into batches to process efficiently
                List<List<Messages>> chunkedMessages = MessagingUtil.chunkList(messages, BroadcastConstants.BATCH_SIZE);

                // Build templates for sending messages
                List<MessageRequest> baseSendTemplate = templateBuilderService.buildSendableTemplates(userId,
                                filteredMobileNumbers, template, dto);

                // Save and process messages
                messagesOrchestratorService.saveAndProcessMessagesReports(chunkedMessages, template,
                                accessTokenCredentials.getId(), accessTokenCredentials.getAccessToken(),
                                baseSendTemplate);

                return new ResponseMessage<>("Success", "BroadCast Initiated successfully", null);
        }

        /**
         * Checks if the broadcast is scheduled.
         */
        private boolean isScheduledBroadcast(BroadcastRequestDTO dto, Broadcast broadcast) {
                return dto.getScheduledAt() != null || broadcast.getStatus() == BroadcastStatus.SCHEDULED;
        }

        /**
         * Returns a response for a scheduled broadcast.
         */
        private ResponseMessage<String> scheduledResponse(LocalDateTime scheduledAt) {
                return new ResponseMessage<>("Success", "Broadcast scheduled successfully at: " + scheduledAt, null);
        }
}
