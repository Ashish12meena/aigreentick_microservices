package com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel;

import java.util.List;

import lombok.Data;

@Data
public class CarouselButtonDto {
    private String type; // quick_reply, url, phone_number
    private String text;
    
    // optional fields depending on type
    private String url;
    private List<String> example; // for URL button variable example
    private String phoneNumber;
}
