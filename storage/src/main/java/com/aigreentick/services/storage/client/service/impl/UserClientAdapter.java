package com.aigreentick.services.storage.client.service.impl;

import org.springframework.stereotype.Component;

import com.aigreentick.services.storage.client.dto.response.AccessTokenCredentials;
import com.aigreentick.services.storage.client.properties.UserClientProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserClientAdapter {

     private final UserClientProperties properties;

    public AccessTokenCredentials getPhoneNumberIdAccessToken(Long userId) {
        if (!properties.isOutgoingEnabled()) {
            
        }
        return new AccessTokenCredentials("669015486305605",
                "EAAOcfziRygMBPOGjGD034ta4V7khOBcoNwZBfFOS3SBrsfFEqsuqhoWrlnZB8a5jXH13dhjsyhg3P6M37pWVe5yrdzryZBSBZCfC1GuFVRLFBQdG1MtZAM4S8aG4oSHvd19uZCGSWfk7pw7bCHhE2tJ1W1O0AHp4jrOMTvHKVNhFpRsDJjrqE9ShWk8h3ZB");
    }

    public AccessTokenCredentials getWabaAccessToken(Long userId) {
        return new AccessTokenCredentials("530819718510685",
                "EAAOcfziRygMBPOGjGD034ta4V7khOBcoNwZBfFOS3SBrsfFEqsuqhoWrlnZB8a5jXH13dhjsyhg3P6M37pWVe5yrdzryZBSBZCfC1GuFVRLFBQdG1MtZAM4S8aG4oSHvd19uZCGSWfk7pw7bCHhE2tJ1W1O0AHp4jrOMTvHKVNhFpRsDJjrqE9ShWk8h3ZB");
    }
}