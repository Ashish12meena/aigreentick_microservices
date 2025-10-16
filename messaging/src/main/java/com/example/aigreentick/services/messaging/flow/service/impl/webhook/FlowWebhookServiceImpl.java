package com.example.aigreentick.services.messaging.flow.service.impl.webhook;


import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.aigreentick.services.messaging.flow.dto.request.FlowEnvelopeDto;
import com.example.aigreentick.services.messaging.flow.service.impl.FlowProcessingService;
import com.example.aigreentick.services.messaging.flow.service.impl.HmacVerifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlowWebhookServiceImpl {
    private final HmacVerifier hmacVerifier;
    private final ObjectMapper objectMapper;
    private final FlowProcessingService flowProcessingService;

    public ResponseEntity<String> processFlowWebhookData(String signature, String body) {
        try {
            // 1 Verify request origin
            if (!hmacVerifier.verify(signature, body)) {
                log.warn("Webhook signature verification failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("signature verification failed");
            }

            JsonNode root = objectMapper.readTree(body);

            // 2 Case A: direct envelope
            if (root.has("encrypted_flow_data") && root.has("encrypted_aes_key")) {
                FlowEnvelopeDto envelope = objectMapper.treeToValue(root, FlowEnvelopeDto.class);
                if (root.has("from")) {
                    envelope.setWaId(root.get("from").asText());
                }
                String response = flowProcessingService.processEnvelope(envelope);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
            }

            // 3 Case B: webhook wrapper with entry->changes->value->messages
            if (root.has("entry")) {
                for (JsonNode entry : root.get("entry")) {
                    JsonNode changes = entry.get("changes");
                    if (changes == null)
                        continue;

                    for (JsonNode change : changes) {
                        JsonNode value = change.get("value");
                        if (value == null)
                            continue;

                        JsonNode messages = value.get("messages");
                        if (messages == null)
                            continue;

                        for (JsonNode message : messages) {
                            String waId = message.has("from") ? message.get("from").asText() : null;
                            JsonNode flowReply = message.has("flow_reply") ? message.get("flow_reply") : null;

                            if (flowReply != null) {
                                FlowEnvelopeDto envelope = objectMapper.treeToValue(flowReply, FlowEnvelopeDto.class);
                                envelope.setWaId(waId);
                                String response = flowProcessingService.processEnvelope(envelope);
                                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
                            }
                        }
                    }
                }
            }

            return ResponseEntity.ok("ignored");

        } catch (IOException e) {
            log.error("Failed to parse webhook body", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid payload");
        } catch (Exception e) {
            log.error("Unhandled error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("server error");
        }
    }
}
