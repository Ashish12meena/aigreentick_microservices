package com.example.aigreentick.services.messaging.flow.config;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.aigreentick.services.messaging.flow.dto.request.FlowResponseDto;
import com.example.aigreentick.services.messaging.flow.service.interfaces.KeyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CryptoService {
    private final KeyProvider keyProvider;
    private final ObjectMapper objectMapper;

    @Value("${whatsappflow.rsa.transformation}")
    private String rsaTransformation;

    @Value("${whatsappflow.response.format}")
    private String responseFormat;

    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;

    public SecretKey decryptEncryptedAesKey(String encryptedAesKeyBase64) throws Exception {
        byte[] encryptedKey = Base64.getDecoder().decode(encryptedAesKeyBase64);
        PrivateKey privateKey = keyProvider.getPrivateKey();
        Cipher rsaCipher = Cipher.getInstance(rsaTransformation);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedKey);
        return new SecretKeySpec(aesKeyBytes, "AES");
    }

    public byte[] decryptAesGcm(SecretKey key, String ivBase64, String ciphertextBase64) throws Exception {
        byte[] cipherWithTag = Base64.getDecoder().decode(ciphertextBase64);
        byte[] iv;
        byte[] cipherBytes;
        if (ivBase64 != null && !ivBase64.isBlank()) {
            iv = Base64.getDecoder().decode(ivBase64);
            cipherBytes = cipherWithTag;
        } else {
            if (cipherWithTag.length <= GCM_IV_LENGTH_BYTES)
                throw new IllegalArgumentException("ciphertext too short");
            iv = java.util.Arrays.copyOfRange(cipherWithTag, 0, GCM_IV_LENGTH_BYTES);
            cipherBytes = java.util.Arrays.copyOfRange(cipherWithTag, GCM_IV_LENGTH_BYTES, cipherWithTag.length);
        }

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher.doFinal(cipherBytes);
    }

    public String encryptResponse(SecretKey key, FlowResponseDto responseDto) throws Exception {
        byte[] iv = java.security.SecureRandom.getInstanceStrong().generateSeed(GCM_IV_LENGTH_BYTES);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] plaintext = objectMapper.writeValueAsBytes(responseDto);
        byte[] ciphertext = cipher.doFinal(plaintext);

        // prepare outputs
        String encryptedOnly = Base64.getEncoder().encodeToString(ciphertext);
        String ivB64 = Base64.getEncoder().encodeToString(iv);

        if ("base64_blob".equalsIgnoreCase(responseFormat)) {
            ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);
            return Base64.getEncoder().encodeToString(buf.array());
        }

        var node = objectMapper.createObjectNode();
        node.put("encrypted_response", encryptedOnly);
        node.put("initial_vector", ivB64);
        node.put("format", "aes_gcm_base64_fields");
        return objectMapper.writeValueAsString(node);
    }
}
