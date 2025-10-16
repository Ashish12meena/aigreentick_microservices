package com.example.aigreentick.services.messaging.flow.service.impl;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HmacVerifier {
    @Value("${whatsappflow.verify.hmac-enabled}")
    private boolean enabled;

    @Value("${whatsappflow.verify.app-secret}")
    private String appSecret;

    @Value("${whatsappflow.verify.hmac-header-name:X-Hub-Signature-256}")
    private String headerName;

    public boolean verify(String signatureHeader, String body) {
        if (!enabled)
            return true;
        if (signatureHeader == null || signatureHeader.isBlank())
            return false;
        try {
            String expected = "sha256=" + hmacHex(appSecret, body);
            return expected.equals(signatureHeader);
        } catch (Exception e) {
            return false;
        }
    }

    private String hmacHex(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : raw)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
