package com.example.aigreentick.services.messaging.flow.service.impl;


import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aigreentick.services.messaging.flow.dto.request.FlowEnvelopeDto;
import com.example.aigreentick.services.messaging.flow.dto.request.FlowRequestDto;
import com.example.aigreentick.services.messaging.flow.dto.request.FlowResponseDto;
import com.example.aigreentick.services.messaging.flow.model.FlowSubmission;
import com.example.aigreentick.services.messaging.flow.repository.FlowSubmissionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlowProcessingService {
    private final com.example.aigreentick.services.messaging.flow.config.CryptoService cryptoService;
    private final ObjectMapper objectMapper;
    private final FlowSubmissionRepository submissionRepository;
    private final FlowOrchestrator orchestrator;

    @Transactional
    public String processEnvelope(FlowEnvelopeDto envelope) throws Exception {
        // 1) decrypt AES key with RSA
        SecretKey aesKey = cryptoService.decryptEncryptedAesKey(envelope.getEncryptedAesKey());

        // 2) decrypt AES-GCM payload
        byte[] plaintext = cryptoService.decryptAesGcm(aesKey, envelope.getInitialVector(),
                envelope.getEncryptedFlowData());
        String json = new String(plaintext, StandardCharsets.UTF_8);

        // 3) parse request
        FlowRequestDto requestDto = objectMapper.readValue(json, FlowRequestDto.class);

        // 4) persist raw submission for audit
        FlowSubmission submission = FlowSubmission.builder()
                .waId(envelope.getWaId())
                .flowToken(requestDto.getFlowToken())
                .flowId(requestDto.getFlowId())
                .screen(requestDto.getScreen())
                .action(requestDto.getAction())
                .payloadJson(json)
                .rawEncryptedJson(envelope.toString())
                .status(FlowSubmission.Status.RECEIVED)
                .createdAt(Instant.now())
                .build();
        submission = submissionRepository.save(submission);

        // 5) route to appropriate handler  means based on data we perform other operations
        FlowResponseDto responseDto = orchestrator.route(requestDto, submission);

        // 6) update submission
        submission.setStatus(FlowSubmission.Status.PROCESSED);
        submission.setProcessedAt(Instant.now());
        submissionRepository.save(submission);

        // 7) encrypt response and return
        String encryptedResponse = cryptoService.encryptResponse(aesKey, responseDto);
        log.info("Processed submission id={} wa={} flowToken={}", submission.getId(), envelope.getWaId(),
                requestDto.getFlowToken());
        return encryptedResponse;
    }
}
