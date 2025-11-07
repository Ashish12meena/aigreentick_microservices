

// CircuitBreakerConfiguration.java
package com.aigreentick.services.notification.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aigreentick.services.notification.config.properties.EmailProviderCircuitBreakerProperties;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CircuitBreakerConfiguration {
    
    private final EmailProviderCircuitBreakerProperties circuitBreakerProperties;
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
    
    @Bean
    public CircuitBreaker smtpCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(circuitBreakerProperties.getSlidingWindowSize())
                .minimumNumberOfCalls(circuitBreakerProperties.getMinimumNumberOfCalls())
                .failureRateThreshold(circuitBreakerProperties.getFailureRateThreshold())
                .slowCallRateThreshold(circuitBreakerProperties.getSlowCallRateThreshold())
                .slowCallDurationThreshold(
                        Duration.ofMillis(circuitBreakerProperties.getSlowCallDurationThresholdMs()))
                .waitDurationInOpenState(
                        Duration.ofMillis(circuitBreakerProperties.getWaitDurationInOpenStateMs()))
                .permittedNumberOfCallsInHalfOpenState(
                        circuitBreakerProperties.getPermittedNumberOfCallsInHalfOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(
                        circuitBreakerProperties.isAutomaticTransitionFromOpenToHalfOpenEnabled())
                .build();
        
        CircuitBreaker circuitBreaker = registry.circuitBreaker("smtpProvider", config);
        
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                        log.warn("SMTP Circuit Breaker state changed from {} to {}", 
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()))
                .onError(event -> 
                        log.error("SMTP Circuit Breaker recorded error: {}", 
                                event.getThrowable().getMessage()));
        
        log.info("SMTP Circuit Breaker initialized - slidingWindowSize: {}, minimumCalls: {}, failureThreshold: {}%",
                circuitBreakerProperties.getSlidingWindowSize(),
                circuitBreakerProperties.getMinimumNumberOfCalls(),
                circuitBreakerProperties.getFailureRateThreshold());
        
        return circuitBreaker;
    }
}
