package com.aigreentick.services.storage.client.service.interfaces;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.aigreentick.services.storage.client.config.FeignClientConfig;
import com.aigreentick.services.storage.client.dto.response.AccessTokenCredentials;
import com.aigreentick.services.storage.client.fallback.UserClientFallback;

@FeignClient(name = "user-service", url = "${user.service.base-url}", 
configuration = FeignClientConfig.class,
 fallback = UserClientFallback.class                                                                                                                                   // resilience
)
public interface UserClient {
    @GetMapping("/access-token/phone")
    AccessTokenCredentials getPhoneNumberIdAccessToken();

    @GetMapping("/access-token/waba")
    AccessTokenCredentials getWabaAccessToken();

   
}
