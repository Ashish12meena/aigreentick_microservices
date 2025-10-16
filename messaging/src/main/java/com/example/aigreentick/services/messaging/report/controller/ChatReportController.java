package com.example.aigreentick.services.messaging.report.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.aigreentick.services.messaging.report.constants.ChatReportConstants;
import com.example.aigreentick.services.messaging.report.dto.ReportResponseDto;
import com.example.aigreentick.services.messaging.report.dto.ReportSummaryDto;
import com.example.aigreentick.services.messaging.report.service.impl.ReportOrchestratorServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ChatReportConstants.Paths.BASE)
@RequiredArgsConstructor
public class ChatReportController {
    private final ReportOrchestratorServiceImpl reportService;

    @GetMapping(ChatReportConstants.Paths.USER_FILTERED)
    public ResponseEntity<Page<ReportResponseDto>> getFilteredReportForLoginUser(
            @RequestParam(defaultValue = ChatReportConstants.Defaults.PAGE) int page,
            @RequestParam(defaultValue = ChatReportConstants.Defaults.SIZE) int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            Long userId) {

        Page<ReportResponseDto> reports = reportService.getFilteredReportsForUser(
                userId, page, size, status, type, fromDate, toDate);
        return ResponseEntity.ok(reports);
    }

    @GetMapping(ChatReportConstants.Paths.MY_SUMMARY)
    public ResponseEntity<ReportSummaryDto> getMyReportSummary(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            Long userId) {

        ReportSummaryDto summary = reportService.getReportSummaryForUser(
                userId, type, fromDate, toDate);
        return ResponseEntity.ok(summary);
    }

}
