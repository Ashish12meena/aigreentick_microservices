package com.example.aigreentick.services.messaging.report.service.impl;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.example.aigreentick.services.messaging.message.enums.MessageStatus;
import com.example.aigreentick.services.messaging.message.enums.MessageType;
import com.example.aigreentick.services.messaging.message.model.Messages;
import com.example.aigreentick.services.messaging.message.service.impl.MessagesServiceImpl;
import com.example.aigreentick.services.messaging.report.constants.ChatReportConstants;
import com.example.aigreentick.services.messaging.report.dto.ReportResponseDto;
import com.example.aigreentick.services.messaging.report.dto.ReportSummaryDto;
import com.example.aigreentick.services.messaging.report.mapper.ReportMapper;
import com.mongodb.client.result.UpdateResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportOrchestratorServiceImpl {

    private final MongoTemplate mongoTemplate;
    private final MessagesServiceImpl messageService;
    private final ReportMapper reportMapper;

    // --- Get reports by user ---
    public Page<ReportResponseDto> getReportsByUserId(Long userId, Pageable pageable) {
        log.info("Fetching reports for userId={}", userId);
        Page<Messages> reportPage = messageService.findByUserIdAndBroadcastIdNotNull(userId, pageable);
        return reportPage.map(reportMapper::toReportDto);
    }

    // --- Filtered reports for user ---
    public Page<ReportResponseDto> getFilteredReportsForUser(Long userId, int page, int size,
            String statusStr, String typeStr, LocalDateTime fromDate, LocalDateTime toDate) {

        log.info("Filtering reports for userId={}, status={}, type={}, from={}, to={}", userId, statusStr, typeStr,
                fromDate, toDate);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, ChatReportConstants.Fields.CREATED_AT));
        Criteria criteria = Criteria.where(ChatReportConstants.Fields.USER_ID).is(userId)
                .and(ChatReportConstants.Fields.BROADCAST_ID).ne(null);

        if (statusStr != null)
            criteria.and(ChatReportConstants.Fields.STATUS).is(MessageStatus.valueOf(statusStr));
        if (typeStr != null)
            criteria.and(ChatReportConstants.Fields.TYPE).is(MessageType.valueOf(typeStr));
        if (fromDate != null && toDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).gte(fromDate).lte(toDate);
        else if (fromDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).gte(fromDate);
        else if (toDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).lte(toDate);

        Query query = new Query(criteria).with(pageable);
        List<Messages> reports = mongoTemplate.find(query, Messages.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Messages.class);

        log.debug("Found {} reports matching filter criteria", reports.size());

        List<ReportResponseDto> dtos = reports.stream().map(reportMapper::toReportDto).toList();
        return new PageImpl<>(dtos, pageable, total);
    }

    // --- Filtered reports by campaign ---
    public Page<ReportResponseDto> getFilteredReportsByCampaign(Long campaignId, int page, int size,
            String statusStr, String typeStr, LocalDateTime fromDate, LocalDateTime toDate) {

        log.info("Filtering reports for campaignId={}, status={}, type={}, from={}, to={}", campaignId, statusStr,
                typeStr, fromDate, toDate);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, ChatReportConstants.Fields.CREATED_AT));
        Criteria criteria = Criteria.where(ChatReportConstants.Fields.CAMPAIGN_ID).is(campaignId)
                .and(ChatReportConstants.Fields.BROADCAST_ID).ne(null);

        if (statusStr != null)
            criteria.and(ChatReportConstants.Fields.STATUS).is(MessageStatus.valueOf(statusStr));
        if (typeStr != null)
            criteria.and(ChatReportConstants.Fields.TYPE).is(MessageType.valueOf(typeStr));
        if (fromDate != null && toDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).gte(fromDate).lte(toDate);
        else if (fromDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).gte(fromDate);
        else if (toDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).lte(toDate);

        Query query = new Query(criteria).with(pageable);
        List<Messages> reports = mongoTemplate.find(query, Messages.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Messages.class);

        log.debug("Found {} reports for campaign", reports.size());

        List<ReportResponseDto> dtos = reports.stream().map(reportMapper::toReportDto).toList();
        return new PageImpl<>(dtos, pageable, total);
    }

    // --- Get reports by broadcast ---
    public Page<ReportResponseDto> getReportsByBroadcastId(Long broadcastId, Pageable pageable) {
        log.info("Fetching reports for broadcastId={}", broadcastId);
        Page<Messages> reports = messageService.findByBroadcastId(broadcastId, pageable);
        return reports.map(reportMapper::toReportDto);
    }

    // --- Report summary for user ---
    public ReportSummaryDto getReportSummaryForUser(Long userId, String typeStr, LocalDateTime fromDate,
            LocalDateTime toDate) {

        log.info("Generating report summary for userId={}, type={}, from={}, to={}", userId, typeStr, fromDate,
                toDate);

        Criteria baseCriteria = Criteria.where(ChatReportConstants.Fields.USER_ID).is(userId)
                .and(ChatReportConstants.Fields.BROADCAST_ID).ne(null);
        if (typeStr != null)
            baseCriteria.and(ChatReportConstants.Fields.TYPE).is(MessageType.valueOf(typeStr));
        if (fromDate != null && toDate != null)
            baseCriteria.and(ChatReportConstants.Fields.CREATED_AT).gte(fromDate).lte(toDate);
        else if (fromDate != null)
            baseCriteria.and(ChatReportConstants.Fields.CREATED_AT).gte(fromDate);
        else if (toDate != null)
            baseCriteria.and(ChatReportConstants.Fields.CREATED_AT).lte(toDate);

        Map<String, Long> statusCounts = new HashMap<>();
        for (MessageStatus status : MessageStatus.values()) {
            Criteria statusCriteria = new Criteria().andOperator(baseCriteria,
                    Criteria.where(ChatReportConstants.Fields.STATUS).is(status));
            long count = mongoTemplate.count(new Query(statusCriteria), Messages.class);
            statusCounts.put(status.name().toLowerCase(), count);
        }

        log.debug("Report summary counts: {}", statusCounts);

        return new ReportSummaryDto(
                statusCounts.getOrDefault(ChatReportConstants.Status.PENDING, 0L),
                statusCounts.getOrDefault(ChatReportConstants.Status.FAILED, 0L),
                statusCounts.getOrDefault(ChatReportConstants.Status.SUCCESS, 0L));
    }

    // --- Filtered reports for admin ---
    public Page<ReportResponseDto> getFilteredReportsForAdmin(Long userId, Long broadcastId, Long campaignId,
            String messageId, String statusStr, String typeStr,
            LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {

        log.info("Filtering reports for admin, userId={}, broadcastId={}, campaignId={}, messageId={}, status={}, type={}",
                userId, broadcastId, campaignId, messageId, statusStr, typeStr);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, ChatReportConstants.Fields.CREATED_AT));
        Criteria criteria = Criteria.where(ChatReportConstants.Fields.BROADCAST_ID).ne(null);

        if (userId != null)
            criteria.and(ChatReportConstants.Fields.USER_ID).is(userId);
        if (broadcastId != null)
            criteria.and(ChatReportConstants.Fields.BROADCAST_ID).is(broadcastId);
        if (campaignId != null)
            criteria.and(ChatReportConstants.Fields.CAMPAIGN_ID).is(campaignId);
        if (messageId != null)
            criteria.and(ChatReportConstants.Fields.MESSAGE_ID).is(messageId);
        if (statusStr != null)
            criteria.and(ChatReportConstants.Fields.STATUS).is(MessageStatus.valueOf(statusStr));
        if (typeStr != null)
            criteria.and(ChatReportConstants.Fields.TYPE).is(MessageType.valueOf(typeStr));
        if (fromDate != null && toDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).gte(fromDate).lte(toDate);
        else if (fromDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).gte(fromDate);
        else if (toDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).lte(toDate);

        Query query = new Query(criteria).with(pageable);
        List<Messages> reports = mongoTemplate.find(query, Messages.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Messages.class);

        log.debug("Admin filtered reports count={}", reports.size());

        List<ReportResponseDto> dtos = reports.stream().map(reportMapper::toReportDto).toList();
        return new PageImpl<>(dtos, pageable, total);
    }

    // --- Restore soft-deleted report ---
    public boolean restoreReportById(String reportId) {
        log.info("Restoring reportId={}", reportId);
        Query query = new Query(Criteria.where(ChatReportConstants.Fields.ID).is(reportId)
                .and(ChatReportConstants.Fields.DELETED_AT).ne(null)
                .and(ChatReportConstants.Fields.BROADCAST_ID).ne(null));

        Update update = new Update().unset(ChatReportConstants.Fields.DELETED_AT);
        UpdateResult result = mongoTemplate.updateFirst(query, update, Messages.class);
        log.debug("Restore result for reportId={} = {}", reportId, result.getModifiedCount());
        return result.getModifiedCount() > 0;
    }

    // --- Bulk soft-delete reports ---
    public long bulkSoftDeleteByFilter(String userId, String campaignId, String broadcastId,
            String statusStr, String typeStr, LocalDateTime fromDate, LocalDateTime toDate) {

        log.info(
                "Bulk soft delete reports with filters userId={}, campaignId={}, broadcastId={}, status={}, type={}, from={}, to={}",
                userId, campaignId, broadcastId, statusStr, typeStr, fromDate, toDate);

        Criteria criteria = Criteria.where(ChatReportConstants.Fields.DELETED_AT).is(null)
                .and(ChatReportConstants.Fields.BROADCAST_ID).ne(null);
        if (userId != null)
            criteria.and(ChatReportConstants.Fields.USER_ID).is(userId);
        if (campaignId != null)
            criteria.and(ChatReportConstants.Fields.CAMPAIGN_ID).is(campaignId);
        if (broadcastId != null)
            criteria.and(ChatReportConstants.Fields.BROADCAST_ID).is(broadcastId);
        if (statusStr != null)
            criteria.and(ChatReportConstants.Fields.STATUS).is(MessageStatus.valueOf(statusStr));
        if (typeStr != null)
            criteria.and(ChatReportConstants.Fields.TYPE).is(MessageType.valueOf(typeStr));
        if (fromDate != null && toDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).gte(fromDate).lte(toDate);
        else if (fromDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).gte(fromDate);
        else if (toDate != null)
            criteria.and(ChatReportConstants.Fields.CREATED_AT).lte(toDate);

        Query query = new Query(criteria);
        Update update = new Update().set(ChatReportConstants.Fields.DELETED_AT, LocalDateTime.now());
        UpdateResult result = mongoTemplate.updateMulti(query, update, Messages.class);

        log.debug("Bulk soft-delete modified count={}", result.getModifiedCount());
        return result.getModifiedCount();
    }

    // --- Update report status ---
    public boolean updateReportStatusByMessageId(String messageId, MessageStatus status, LocalDateTime updatedAt) {
        log.info("Updating status for messageId={} to status={}", messageId, status);
        Query query = new Query(Criteria.where(ChatReportConstants.Fields.MESSAGE_ID).is(messageId)
                .and(ChatReportConstants.Fields.DELETED_AT).is(null)
                .and(ChatReportConstants.Fields.BROADCAST_ID).ne(null));

        Update update = new Update()
                .set(ChatReportConstants.Fields.STATUS, status)
                .set(ChatReportConstants.Fields.CREATED_AT, updatedAt != null ? updatedAt : LocalDateTime.now());

        UpdateResult result = mongoTemplate.updateFirst(query, update, Messages.class);
        log.debug("Update status result for messageId={} = {}", messageId, result.getModifiedCount());
        return result.getModifiedCount() > 0;
    }
}
