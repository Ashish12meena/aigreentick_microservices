package com.aigreentick.services.template.dto.buildTemplateForRequest;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class BuildTemplateRequestDto {
    private String languageCode;
    private String otp;
    private String copyCode;
    // private String templateType;
    private boolean isFullyPrameterized;
    private String defaultValue;
    private Map<String,String> parameters;
    private Long expirationTimeMs;
    private boolean isMedia;
    private long mediaId;
    private String mediaUrl;
    private String mediaType;
    List<String> mediaIdsForCarosel;
    private String catalogId;
    private List<String> productRetailerIds;
}
