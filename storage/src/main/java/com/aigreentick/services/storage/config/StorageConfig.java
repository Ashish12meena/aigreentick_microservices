package com.aigreentick.services.storage.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.aigreentick.services.storage.service.StorageProvider;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StorageConfig {
    
    @Value("${storage.active-provider:local}")
    private String activeProvider;
    
    private final List<StorageProvider> storageProviders;
    
    @Bean
    @Primary
    public StorageProvider storageProvider() {
        log.info("Initializing storage provider. Active provider: {}", activeProvider);
        
        Map<String, StorageProvider> providerMap = storageProviders.stream()
                .collect(Collectors.toMap(
                        provider -> provider.getProviderType().name().toLowerCase(),
                        Function.identity()
                ));
        
        StorageProvider provider = providerMap.get(activeProvider.toLowerCase());
        
        if (provider == null) {
            log.error("Storage provider '{}' not found. Available providers: {}", 
                     activeProvider, providerMap.keySet());
            throw new IllegalStateException("Storage provider not configured: " + activeProvider);
        }
        
        log.info("Using storage provider: {} ({})", 
                 provider.getProviderType(), 
                 provider.getProviderType().getDisplayName());
        
        return provider;
    }
}