package com.aigreentick.services.template.client.fallback;

import org.springframework.stereotype.Component;

import com.aigreentick.services.template.client.dto.response.UserResponseDto;
import com.aigreentick.services.template.client.interfaces.UserClient;

@Component
public class UserClientFallback implements UserClient {
    
    @Override
    public UserResponseDto getUserById(Long userId) {
        // Return cached data or default response
        return UserResponseDto.builder()
                .id(userId)
                .userName("Fallback User")
                .build();
    }
}
