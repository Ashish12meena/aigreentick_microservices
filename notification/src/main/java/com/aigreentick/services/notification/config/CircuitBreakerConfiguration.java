package com.aigreentick.services.notification.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class CircuitBreakerConfiguration {
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
    
    @Bean
    public CircuitBreaker smtpCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .slowCallRateThreshold(100)
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
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
        
        return circuitBreaker;
    }
}