package com.example.aigreentick.services.messaging.report.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDto {
    private long pending;
    private long failed;
    private long success;
}
