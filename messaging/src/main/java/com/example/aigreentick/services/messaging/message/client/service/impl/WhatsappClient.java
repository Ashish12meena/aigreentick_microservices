package com.example.aigreentick.services.messaging.message.client.service.impl;

import java.net.URI;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.aigreentick.services.common.dto.response.FacebookApiResponse;
import com.example.aigreentick.services.messaging.message.client.config.WhatsappClientProperties;
import com.example.aigreentick.services.messaging.message.dto.response.SendTemplateMessageResponse;
import com.fasterxml.jackson.databind.JsonNode;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
@RequiredArgsConstructor
public class WhatsappClient {

    private final WebClient.Builder webClientBuilder;
    private final WhatsappClientProperties properties;

    /**
     * Sends a WhatsApp message using a pre-approved template.
     * This method communicates with Facebook Graph API.
     * Wrapped with Retry, CircuitBreaker, and RateLimiter for fault tolerance.
     */

    @Retry(name = "whatsappMessageRetry", fallbackMethod = "sendMessageFallback")
    @CircuitBreaker(name = "whatsappMessageCircuitBreaker", fallbackMethod = "sendMessageFallback")
    @RateLimiter(name = "whatsappMessageRateLimiter", fallbackMethod = "rateLimiterFallback")
    public FacebookApiResponse<SendTemplateMessageResponse> sendMessage(String bodyJson,
            String phoneNumberId, String accessToken) {

        if (!properties.isOutgoingEnabled()) {
            return FacebookApiResponse.error("Outgoing requests disabled", 503);
        }

        URI uri = UriComponentsBuilder
                .fromUriString(properties.getBaseUrl())
                .pathSegment(properties.getApiVersion(), phoneNumberId, "messages")
                .build()
                .toUri();

        try {
            SendTemplateMessageResponse response = webClientBuilder.build()
                    .post()
                    .uri(uri)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .bodyValue(bodyJson)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            r -> Mono.error(new RuntimeException("Facebook API returned 4xx")))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            r -> Mono.error(new RuntimeException("Facebook API returned 5xx")))
                    .bodyToMono(SendTemplateMessageResponse.class)
                    .block();

            log.info("Template message sent. PHONE_NUMBER_ID={} Response={}", phoneNumberId, response);
            return FacebookApiResponse.success(response, 200);

        } catch (WebClientResponseException ex) {
            log.error("Failed to send message. PHONE_NUMBER_ID={} Status={} Response={}",
                    phoneNumberId, ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return FacebookApiResponse.error(ex.getResponseBodyAsString(), ex.getStatusCode().value());

        } catch (Exception ex) {
            log.error("Unexpected error while sending message. PHONE_NUMBER_ID={}", phoneNumberId, ex);
            return FacebookApiResponse.error("Internal Server Error: " + ex.getMessage(), 500);
        }
    }

      // ====== FALLBACKS ======

    @SuppressWarnings("unused")
    private FacebookApiResponse<JsonNode> sendMessageFallback(
            String bodyJson, String phoneNumberId, String accessToken, Throwable ex) {
        log.error("Fallback triggered while sending WhatsApp message. PHONE_NUMBER_ID={}", phoneNumberId, ex);
        return FacebookApiResponse.error("Fallback triggered: " + ex.getMessage(), 500);
    }

    @SuppressWarnings("unused")
    private FacebookApiResponse<JsonNode> rateLimiterFallback(
            String bodyJson, String phoneNumberId, String accessToken, Throwable ex) {
        log.warn("Rate limiter triggered for WhatsApp message sending. PHONE_NUMBER_ID={}", phoneNumberId, ex);
        return FacebookApiResponse.error("Rate limit exceeded. Please retry later.", 429);
    }

}
