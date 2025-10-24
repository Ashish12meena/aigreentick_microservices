package com.example.aigreentick.services.messaging.flow.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.example.aigreentick.services.messaging.flow.service.interfaces.KeyProvider;


@Component
@Primary
public class FileKeyProvider implements KeyProvider {
    @Value("${whatsappflow.private-key-file}")
    private Resource privateKeyResource;

    @Override
    public PrivateKey getPrivateKey() throws Exception {
        String pem = new String(privateKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String cleaned = pem.replaceAll("-----BEGIN (.*)-----", "").replaceAll("-----END (.*)-----", "")
                .replaceAll("\s", "");
        byte[] decoded = Base64.getDecoder().decode(cleaned);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }
}
