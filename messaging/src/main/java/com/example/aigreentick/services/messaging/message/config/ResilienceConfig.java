package com.example.aigreentick.services.messaging.message.config;
import java.io.IOException;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

@Configuration
public class ResilienceConfig {

    // ---------------- Retry Configuration ----------------
    
    /**
     * Default retry configuration used for most service calls.
     * Retries up to 3 times with 500ms wait between attempts.
     * Only retries IOException and WebClientRequestException.
     * Ignores IllegalArgumentException.
     */
    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(IOException.class, WebClientRequestException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
    }

    /**
     * Custom retry configuration specifically for WhatsApp template API calls.
     * Retries up to 5 times with 1 second wait between attempts.
     * Same retry/ignore exceptions as default.
     */
    @Bean
    public RetryConfig whatsappRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofSeconds(1))
                .retryExceptions(IOException.class, WebClientRequestException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
    }

    /**
     * Registry to hold all retry instances.
     * Default configuration is applied to unnamed retries.
     */
    @Bean
    public RetryRegistry retryRegistry(RetryConfig config) {
        return RetryRegistry.of(config);
    }

    /**
     * Named Retry for WhatsApp template calls using custom configuration.
     * Can be injected directly into services where WhatsApp calls are made.
     */
    @Bean
    public Retry whatsappTemplateRetry(RetryRegistry registry, RetryConfig whatsappRetryConfig) {
        return registry.retry("whatsappMessageRetry", whatsappRetryConfig);
    }

    // ---------------- CircuitBreaker Configuration ----------------
    
    /**
     * Default CircuitBreaker configuration for general service calls.
     * Opens the circuit if 50% of last 20 calls fail or are slow (>2s).
     * Waits 10 seconds in open state before trying half-open.
     */
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

    /**
     * Custom CircuitBreaker configuration for WhatsApp template API calls.
     * Slightly stricter thresholds and faster open/half-open transitions.
     */
    @Bean
    public CircuitBreakerConfig whatsappCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(30)
                .failureRateThreshold(40f)
                .slowCallRateThreshold(40f)
                .slowCallDurationThreshold(Duration.ofSeconds(1))
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .minimumNumberOfCalls(5)
                .build();
    }

    /**
     * Registry to hold all CircuitBreaker instances.
     * Default configuration is applied to unnamed CircuitBreakers.
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig cbConfig) {
        return CircuitBreakerRegistry.of(cbConfig);
    }
    
    /**
     * Named CircuitBreaker specifically for WhatsApp template calls.
     * Uses custom configuration for stricter reliability handling.
     */
    @Bean
    public CircuitBreaker whatsappTemplateCircuitBreaker(CircuitBreakerRegistry registry,
                                                         CircuitBreakerConfig whatsappCircuitBreakerConfig) {
        return registry.circuitBreaker("whatsappMessageCircuitBreaker", whatsappCircuitBreakerConfig);
    }

    // ---------------- RateLimiter Configuration ----------------
    
    /**
     * Default RateLimiter configuration for general service calls.
     * Allows up to 10 requests per second, fails fast if limit is exceeded.
     */
    @Bean
    public RateLimiterConfig defaultRateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();
    }

    /**
     * Custom RateLimiter configuration for WhatsApp template API calls.
     * Allows higher throughput of 20 requests per second.
     */
    @Bean
    public RateLimiterConfig whatsappRateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(20)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();
    }

    /**
     * Registry to hold all RateLimiter instances.
     * Default configuration is applied to unnamed RateLimiters.
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig defaultRateLimiterConfig) {
        return RateLimiterRegistry.of(defaultRateLimiterConfig);
    }

    /**
     * Named RateLimiter specifically for WhatsApp template calls.
     * Can be used to throttle requests and protect WhatsApp API from overload.
     */
    @Bean
    public RateLimiter whatsappTemplateRateLimiter(RateLimiterRegistry registry,
                                                   RateLimiterConfig whatsappRateLimiterConfig) {
        return registry.rateLimiter("whatsappMessageRateLimiter", whatsappRateLimiterConfig);
    }
}
