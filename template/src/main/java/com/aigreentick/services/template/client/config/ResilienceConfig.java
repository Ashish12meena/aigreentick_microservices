package com.aigreentick.services.template.client.config;

import java.io.IOException;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

@Configuration
public class ResilienceConfig {

    // ---------------- Default Retry Configuration ----------------

    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(3000))
                .retryExceptions(IOException.class, WebClientRequestException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
    }

    @Bean
    public RetryRegistry retryRegistry(RetryConfig defaultRetryConfig) {
        return RetryRegistry.of(defaultRetryConfig);
    }

    @Bean
    public Retry defaultRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry("defaultRetry");
    }

    // ---------------- WhatsApp Retry Configuration ----------------

    @Bean
    public RetryConfig whatsappRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(15)) // longer wait for FB API
                .retryExceptions(IOException.class, WebClientRequestException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
    }

    @Bean
    public Retry whatsappRetry(RetryRegistry retryRegistry) {
        // create or override "whatsappTemplateRetry" in the same registry
        return retryRegistry.retry("whatsappTemplateRetry", whatsappRetryConfig());
    }

    // ---------------- Default CircuitBreaker Configuration ----------------

    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .failureRateThreshold(50f)
                .slowCallRateThreshold(50f)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(5)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .minimumNumberOfCalls(10)
                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultCircuitBreakerConfig) {
        return CircuitBreakerRegistry.of(defaultCircuitBreakerConfig);
    }

    // ---------------- WhatsApp CircuitBreaker Configuration ----------------

    @Bean
    public CircuitBreakerConfig whatsappCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .failureRateThreshold(50f)
                .slowCallRateThreshold(50f)
                .slowCallDurationThreshold(Duration.ofSeconds(15)) // slower threshold for FB API
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .permittedNumberOfCallsInHalfOpenState(5)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .minimumNumberOfCalls(5)
                .build();
    }

    @Bean
    public io.github.resilience4j.circuitbreaker.CircuitBreaker whatsappCircuitBreaker(
            CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker("whatsappTemplateCircuitBreaker", whatsappCircuitBreakerConfig());
    }

    // ---------------- Default RateLimiter Configuration ----------------

    @Bean
    public RateLimiterConfig defaultRateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig defaultRateLimiterConfig) {
        return RateLimiterRegistry.of(defaultRateLimiterConfig);
    }

    // ---------------- WhatsApp RateLimiter Configuration ----------------

    @Bean
    public RateLimiterConfig whatsappRateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(25) // smaller limit for WhatsApp API
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofSeconds(2))
                .build();
    }

    @Bean
    public io.github.resilience4j.ratelimiter.RateLimiter whatsappRateLimiter(
            RateLimiterRegistry rateLimiterRegistry) {
        return rateLimiterRegistry.rateLimiter("whatsappTemplateRateLimiter", whatsappRateLimiterConfig());
    }
}
