package com.example.aigreentick.services.messaging.message.client.service.impl;

import java.net.URI;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.aigreentick.services.messaging.message.client.config.PhoneBookClientProperties;
import com.example.aigreentick.services.messaging.message.client.dto.PhoneBookResponseDto;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class PhoneBookEntryCleint {
    private final WebClient.Builder webClientBuilder;
    private final PhoneBookClientProperties properties;

    /**
     * Fetch dynamic parameters for given phone numbers and keys.
     * Communicates with the internal PhoneBook microservice.
     */

    @Retry(name = "phoneBookRetry", fallbackMethod = "getParamsFallback")
    @CircuitBreaker(name = "phoneBookCircuitBreaker", fallbackMethod = "getParamsFallback")
    @RateLimiter(name = "phoneBookRateLimiter", fallbackMethod = "rateLimiterFallback")
    public PhoneBookResponseDto getParamsForPhoneNumbers(List<String> phoneNumbers,
            List<String> keys, Long userId, String defaultValue) {

        URI uri = UriComponentsBuilder
                .fromUriString(properties.getBaseUrl())
                .pathSegment(properties.getApiVersion(), "phonebook", "params")
                .queryParam("userId", userId)
                .queryParam("defaultValue", defaultValue)
                .build()
                .toUri();

        try {
            var requestBody = new PhoneBookRequest(phoneNumbers, keys);

            PhoneBookResponseDto response = webClientBuilder.build()
                    .post()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            r -> Mono.error(new RuntimeException("PhoneBook API returned 4xx")))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            r -> Mono.error(new RuntimeException("PhoneBook API returned 5xx")))
                    .bodyToMono(new ParameterizedTypeReference<PhoneBookResponseDto>() {
                    })
                    .block();

            log.info("Fetched {} phone entries for userId={}", response.getData().size(), userId);
            return response;

        } catch (WebClientResponseException ex) {
            log.error("Failed to fetch phone params. UserID={} Status={} Response={}",
                    userId, ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new RuntimeException("PhoneBook service error: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error fetching phone params. UserID={}", userId, ex);
            throw new RuntimeException("Internal error while fetching phone params: " + ex.getMessage());
        }
    }
    // ==== FALLBACKS ====

    @SuppressWarnings("unused")
    private PhoneBookResponseDto getParamsFallback(
            List<String> phoneNumbers, List<String> keys, Long userId, String defaultValue, Throwable ex) {
        log.error("Fallback triggered for PhoneBook service (userId={}).", userId, ex);
        PhoneBookResponseDto fallback = new PhoneBookResponseDto();
        fallback.setData(phoneNumbers.stream()
                .collect(java.util.stream.Collectors.toMap(
                        num -> num,
                        num -> keys.stream()
                                .collect(java.util.stream.Collectors.toMap(k -> k, k -> defaultValue)))));
        return fallback;
    }

    @SuppressWarnings("unused")
    private PhoneBookResponseDto rateLimiterFallback(
            List<String> phoneNumbers, List<String> keys, Long userId, String defaultValue, Throwable ex) {
        log.warn("Rate limit triggered for PhoneBook service (userId={}).", userId, ex);
        return getParamsFallback(phoneNumbers, keys, userId, defaultValue, ex);
    }

    // Inner static DTO for request
    private record PhoneBookRequest(List<String> phoneNumbers, List<String> keys) {
    }

}
