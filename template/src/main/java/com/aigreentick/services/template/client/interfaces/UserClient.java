package com.aigreentick.services.template.client.interfaces;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.aigreentick.services.template.client.config.FeignClientConfig;
import com.aigreentick.services.template.client.dto.response.UserResponseDto;
import com.aigreentick.services.template.client.fallback.UserClientFallback;


@FeignClient(
    name = "user-service",
    url = "${user.service.base-url}",
    configuration = FeignClientConfig.class,
    fallback = UserClientFallback.class  // Optional, for resilience
)
public interface UserClient {
    @GetMapping("api/v1/users/{id}")
    UserResponseDto getUserById(@PathVariable("id") Long userId);
}
