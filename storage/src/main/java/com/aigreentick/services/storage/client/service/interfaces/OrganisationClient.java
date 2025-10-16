package com.aigreentick.services.storage.client.service.interfaces;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


import com.aigreentick.services.storage.client.config.FeignClientConfig;
import com.aigreentick.services.storage.client.dto.response.StorageInfo;
import com.aigreentick.services.storage.client.fallback.OrganisationClientFallback;


@FeignClient(name = "organisation-service", url = "${organisation.service.base-url}", configuration = FeignClientConfig.class, fallback = OrganisationClientFallback.class)
public interface OrganisationClient {
    @GetMapping("/storage-info")
    StorageInfo getStorageInfo();
}
