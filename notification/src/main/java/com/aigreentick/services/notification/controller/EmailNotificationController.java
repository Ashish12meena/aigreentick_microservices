package com.aigreentick.services.notification.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aigreentick.services.notification.dto.request.email.EmailNotificationControllerRequest;
import com.aigreentick.services.notification.dto.request.email.EmailNotificationRequest;
import com.aigreentick.services.notification.dto.request.email.SendTemplatedEmailRequest;
import com.aigreentick.services.notification.dto.response.EmailNotificationResponse;
import com.aigreentick.services.notification.service.email.impl.EmailOrchestratorServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification/email")
public class EmailNotificationController {
    private final EmailOrchestratorServiceImpl emailOrchestratorservice;

    /**
     * Send email synchronously with multipart support
     */
    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmailNotificationResponse> sendEmail(
            @RequestPart("request") @Valid EmailNotificationControllerRequest request,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachmentFiles,
            @RequestPart(value = "inline", required = false) List<MultipartFile> inlineResources) {
        
        log.info("Received request to send email to: {}", request.getTo());

        EmailNotificationResponse response = emailOrchestratorservice.sendEmail(
                request, attachmentFiles, inlineResources);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Send email asynchronously (legacy thread-pool based)
     */
    @PostMapping("/send/async")
    public ResponseEntity<CompletableFuture<EmailNotificationResponse>> sendEmailAsync(
            @Valid @RequestBody EmailNotificationRequest request) {
        
        log.info("Received request to send email asynchronously to: {}", request.getTo());

        CompletableFuture<EmailNotificationResponse> response = 
                emailOrchestratorservice.sendEmailAsync(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }


    /**
     * Send templated email synchronously
     */
    @PostMapping("/send/templated")
    public ResponseEntity<EmailNotificationResponse> sendTemplatedEmail(
            @Valid @RequestBody SendTemplatedEmailRequest request) {
        
        log.info("Received request to send templated email to: {} with template: {}",
                request.getTo(), request.getTemplateCode());

        EmailNotificationResponse response = emailOrchestratorservice.sendTemplatedEmail(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}