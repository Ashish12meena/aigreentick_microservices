package com.example.aigreentick.services.messaging.flow.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aigreentick.services.messaging.flow.service.impl.webhook.FlowWebhookServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/flows")
@Slf4j
public class FlowWebhookController {
    private final FlowWebhookServiceImpl flowWebhookService;

    @PostMapping(path = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> receiveWebhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String body) {
        // Delegate everything to the service
        return flowWebhookService.processFlowWebhookData(signature, body);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}
