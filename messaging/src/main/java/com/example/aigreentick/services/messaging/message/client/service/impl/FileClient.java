package com.example.aigreentick.services.messaging.message.client.service.impl;

import java.net.URI;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.aigreentick.services.messaging.message.client.config.FileClientProperties;
import com.example.aigreentick.services.messaging.message.exceptions.OutgoingRequestsDisabledException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class FileClient {

    private final WebClient.Builder webClientBuilder;
    private final FileClientProperties properties;

    @Retry(name = "fileServiceRetry", fallbackMethod = "getMediaIdByIdFallback")
    @CircuitBreaker(name = "fileServiceCircuitBreaker", fallbackMethod = "getMediaIdByIdFallback")
    @RateLimiter(name = "fileServiceRateLimiter", fallbackMethod = "rateLimiterFallback")
    public String getMediaIdById(long mediaId) {

        if (!properties.isOutgoingEnabled()) {
            log.warn("Outgoing requests to File service are disabled. mediaId={}", mediaId);
            throw new OutgoingRequestsDisabledException("Outgoing requests to File service are disabled");
        }

        URI uri = UriComponentsBuilder
                .fromUriString(properties.getBaseUrl())
                .pathSegment(properties.getApiVersion(), "files", String.valueOf(mediaId), "media")
                .build()
                .toUri();

        try {
            String mediaHandle = webClientBuilder.build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            r -> Mono.error(new RuntimeException("File service returned 4xx")))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            r -> Mono.error(new RuntimeException("File service returned 5xx")))
                    .bodyToMono(String.class)
                    .block();

            log.info("Fetched mediaId={} from File service for media record ID={}", mediaHandle, mediaId);
            return mediaHandle;

        } catch (WebClientResponseException ex) {
            log.error("Failed to fetch media ID. mediaId={} Status={} Response={}",
                    mediaId, ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new RuntimeException("File service error: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error calling File service. mediaId={}", mediaId, ex);
            throw new RuntimeException("Internal error while fetching mediaId: " + ex.getMessage());
        }
    }

    // ======= FALLBACKS =======

    @SuppressWarnings("unused")
    private String getMediaIdByIdFallback(long mediaId, Throwable ex) {
        log.error("Fallback triggered while fetching media ID for mediaId={}", mediaId, ex);
        return null; // or "UNKNOWN_MEDIA_ID"
    }

    @SuppressWarnings("unused")
    private String rateLimiterFallback(long mediaId, Throwable ex) {
        log.warn("Rate limiter triggered for File service while fetching mediaId={}", mediaId, ex);
        return null;
    }

}
