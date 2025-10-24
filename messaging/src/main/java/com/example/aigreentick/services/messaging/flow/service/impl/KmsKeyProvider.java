package com.example.aigreentick.services.messaging.flow.service.impl;


import java.security.PrivateKey;

import org.springframework.stereotype.Component;

import com.example.aigreentick.services.messaging.flow.service.interfaces.KeyProvider;


@Component
public class KmsKeyProvider implements KeyProvider {
    @Override
    public PrivateKey getPrivateKey() throws Exception {
        // Implement your cloud KMS retrieval logic here (AWS KMS, GCP KMS, Azure Key
        // Vault)
        throw new UnsupportedOperationException("KMS key provider not implemented. Implement per your cloud provider.");
    }
}
