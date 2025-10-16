package com.example.aigreentick.services.messaging.report.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.example.aigreentick.services.messaging.message.enums.MessageStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private Long broadCastId;
    private Long campaignId;
    private String mobile;
    private String messageId;
    private String waId;
    private MessageStatus messageStatus;
    private Map<String, Object> response;
    private String to;
    private String from;
    private MessageStatus status;
    private LocalDateTime sendAt;
}
