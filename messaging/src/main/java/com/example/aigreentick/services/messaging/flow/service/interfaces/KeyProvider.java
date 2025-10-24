package com.example.aigreentick.services.messaging.flow.service.interfaces;

import java.security.PrivateKey;

public interface KeyProvider {
    PrivateKey getPrivateKey() throws Exception;
}
