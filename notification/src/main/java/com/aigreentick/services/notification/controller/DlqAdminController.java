// src/main/java/com/aigreentick/services/notification/controller/DlqAdminController.java
package com.aigreentick.services.notification.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aigreentick.services.notification.dto.request.DlqRetryRequest;
import com.aigreentick.services.notification.dto.response.DlqStats;
import com.aigreentick.services.notification.model.entity.DlqMessage;
import com.aigreentick.services.notification.service.dlq.DlqAdminService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Admin controller for managing Dead Letter Queue messages
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dlq")
@RequiredArgsConstructor
public class DlqAdminController {

    private final DlqAdminService dlqAdminService;

    /**
     * Get all unprocessed DLQ messages
     */
    @GetMapping("/unprocessed")
    public ResponseEntity<Page<DlqMessage>> getUnprocessedMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DlqMessage> messages = dlqAdminService.getUnprocessedMessages(pageable);
        
        return ResponseEntity.ok(messages);
    }

    /**
     * Get DLQ message by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DlqMessage> getDlqMessageById(@PathVariable String id) {
        DlqMessage message = dlqAdminService.getDlqMessageById(id);
        return ResponseEntity.ok(message);
    }

    /**
     * Get DLQ statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<DlqStats> getDlqStats() {
        DlqStats stats = dlqAdminService.getDlqStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Retry a single DLQ message
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<String> retryDlqMessage(
            @PathVariable String id,
            @RequestBody DlqRetryRequest request) {
        
        log.info("Retry DLQ message requested: {} by {}", id, request.getRequestedBy());
        
        dlqAdminService.retryDlqMessage(id, request);
        
        return ResponseEntity.ok("DLQ message retry initiated: " + id);
    }

    /**
     * Retry multiple DLQ messages
     */
    @PostMapping("/retry/batch")
    public ResponseEntity<String> retryDlqMessagesBatch(
            @RequestBody DlqRetryRequest request) {
        
        log.info("Batch DLQ retry requested by: {}", request.getRequestedBy());
        
        int retried = dlqAdminService.retryUnprocessedMessages(request);
        
        return ResponseEntity.ok("Batch retry initiated for " + retried + " messages");
    }

    /**
     * Mark DLQ message as processed without retry
     */
    @PostMapping("/{id}/mark-processed")
    public ResponseEntity<String> markAsProcessed(
            @PathVariable String id,
            @RequestBody DlqRetryRequest request) {
        
        log.info("Mark DLQ message as processed: {} by {}", id, request.getRequestedBy());
        
        dlqAdminService.markAsProcessed(id, request.getRequestedBy(), request.getNotes());
        
        return ResponseEntity.ok("DLQ message marked as processed: " + id);
    }
}