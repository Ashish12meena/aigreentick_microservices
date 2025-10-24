package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing;



import lombok.Data;


@Data
public class LimitedTimeOfferDto {
    private String text;
    private boolean hasExpiration;
}
