package com.aigreentick.services.storage.validator;

import org.springframework.stereotype.Component;

import com.aigreentick.services.storage.client.dto.response.StorageInfo;
import com.aigreentick.services.storage.client.service.impl.OrganisationClientAdapter;
import com.aigreentick.services.storage.exception.StorageLimitExceedException;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Component
public class ClientValidator {
    private final OrganisationClientAdapter userClient;

    public void validateStorageInfo(long fileSize) {
        StorageInfo storageInfo = userClient.getStorageInfo();
        if (storageInfo.getRemaining() < fileSize) {
            throw new StorageLimitExceedException("Storage of organisation full only remaining "+ storageInfo.getRemaining());
        }

    }
}
