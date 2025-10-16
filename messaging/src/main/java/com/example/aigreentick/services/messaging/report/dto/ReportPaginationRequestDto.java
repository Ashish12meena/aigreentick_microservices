package com.example.aigreentick.services.messaging.report.dto;



import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ReportPaginationRequestDto {

    @Min(0)
    private int page = 0;

    @Min(1)
    private int size = 10;
}