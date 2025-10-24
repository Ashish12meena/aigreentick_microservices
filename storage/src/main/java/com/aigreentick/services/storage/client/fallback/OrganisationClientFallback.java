package com.aigreentick.services.storage.client.fallback;

import org.springframework.stereotype.Component;

import com.aigreentick.services.storage.client.dto.response.StorageInfo;
import com.aigreentick.services.storage.client.service.interfaces.OrganisationClient;

@Component
public class OrganisationClientFallback implements OrganisationClient {

    @Override
    public StorageInfo getStorageInfo() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStorageInfo'");
    }
    
}
