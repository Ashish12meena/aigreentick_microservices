package com.aigreentick.services.notification.provider.selector;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.aigreentick.services.notification.config.properties.PushProperties;
import com.aigreentick.services.notification.enums.push.PushProviderType;
import com.aigreentick.services.notification.exceptions.ProviderNotAvailableException;
import com.aigreentick.services.notification.provider.push.PushProviderStrategy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PushProviderSelector {
    
    private final Map<PushProviderType, PushProviderStrategy> providers;
    private final PushProperties properties;
    
    public PushProviderSelector(List<PushProviderStrategy> providerList,
                                PushProperties properties) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(
                        PushProviderStrategy::getProviderType,
                        Function.identity()));
        this.properties = properties;
        
        log.info("Initialized PushProviderSelector with providers: {}", providers.keySet());
    }
    
    public PushProviderStrategy selectProvider() {
        PushProviderType activeProviderType = properties.getActive();
        
        PushProviderStrategy provider = providers.get(activeProviderType);
        
        if (provider != null && provider.isAvailable()) {
            log.debug("Selected active push provider: {}", activeProviderType);
            return provider;
        }
        
        log.warn("Active provider {} not available, attempting fallback", activeProviderType);
        
        PushProviderStrategy fallbackProvider = providers.values().stream()
                .filter(PushProviderStrategy::isAvailable)
                .findFirst()
                .orElse(null);
        
        if (fallbackProvider != null) {
            log.warn("Using fallback provider: {}", fallbackProvider.getProviderType());
            return fallbackProvider;
        }
        
        throw new ProviderNotAvailableException("No push provider is currently available");
    }
    
    public PushProviderStrategy getProvider(PushProviderType providerType) {
        PushProviderStrategy provider = providers.get(providerType);
        if (provider == null) {
            throw new ProviderNotAvailableException("Provider not found: " + providerType);
        }
        return provider;
    }
    
    public boolean isProviderAvailable(PushProviderType providerType) {
        PushProviderStrategy provider = providers.get(providerType);
        return provider != null && provider.isAvailable();
    }
    
    public Map<PushProviderType, Boolean> getAllProviderStatuses() {
        return providers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().isAvailable()
                ));
    }
}