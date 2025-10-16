package com.aigreentick.services.template.client.impl;

import org.springframework.stereotype.Component;

import com.aigreentick.services.template.client.config.UserClientProperties;
import com.aigreentick.services.template.client.dto.response.AccessTokenCredentials;
import com.aigreentick.services.template.client.dto.response.UserResponseDto;
import com.aigreentick.services.template.client.interfaces.UserClient;
import com.aigreentick.services.template.exception.ExternalServiceException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserClientImpl {
    private final UserClient userClient;
    private final UserClientProperties properties;

    @Retry(name = "userClientRetry", fallbackMethod = "getUserByIdFallback")
    @CircuitBreaker(name = "userClientCircuitBreaker", fallbackMethod = "getUserByIdFallback")
    public UserResponseDto findById(Long userId) {
        if (!properties.isOutgoingEnabled()) {
            return getUserByIdFallback(userId, new RuntimeException("User service is disabled"));
        }
        return userClient.getUserById(userId);
    }

    // For removal
    public AccessTokenCredentials getWabaAccessToken() {
        return new AccessTokenCredentials("1930966694415732",
                "EAAOcfziRygMBPOGjGD034ta4V7khOBcoNwZBfFOS3SBrsfFEqsuqhoWrlnZB8a5jXH13dhjsyhg3P6M37pWVe5yrdzryZBSBZCfC1GuFVRLFBQdG1MtZAM4S8aG4oSHvd19uZCGSWfk7pw7bCHhE2tJ1W1O0AHp4jrOMTvHKVNhFpRsDJjrqE9ShWk8h3ZB");
    }

    public boolean validateUserExists(Long userId) {
        try {
            return findById(userId) != null;
        } catch (ExternalServiceException ex) {
            return false;
        }
    }

    public UserResponseDto getUserByIdFallback(Long userId, Throwable t) {
        // handle fallback
        return new UserResponseDto();
    }

}
