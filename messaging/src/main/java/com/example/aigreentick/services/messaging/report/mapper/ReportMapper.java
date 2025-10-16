package com.example.aigreentick.services.messaging.report.mapper;



import java.util.List;

import org.springframework.stereotype.Component;

import com.example.aigreentick.services.messaging.message.model.Messages;
import com.example.aigreentick.services.messaging.report.dto.ReportResponseDto;


@Component
public class ReportMapper {
     public ReportResponseDto toReportDto(Messages report) {
        ReportResponseDto reportResponseDto = new ReportResponseDto();
        reportResponseDto.setBroadCastId(report.getBroadcastId());
        reportResponseDto.setCampaignId(report.getCampaignId());
        reportResponseDto.setMessageId(report.getMessageId());
        reportResponseDto.setWaId(report.getWaId());
        reportResponseDto.setTo(report.getTo());
        reportResponseDto.setFrom(report.getFrom());
        reportResponseDto.setStatus(report.getStatus());
        reportResponseDto.setSendAt(report.getCreatedAt());
        return reportResponseDto;
    }


    public List<ReportResponseDto> toReportDtoList(List<Messages> reports) {
        return reports.stream().map(this::toReportDto).toList();
    }
}

