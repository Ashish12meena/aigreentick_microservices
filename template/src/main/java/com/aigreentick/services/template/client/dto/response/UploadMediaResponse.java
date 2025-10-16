package com.aigreentick.services.template.client.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UploadMediaResponse {
    @JsonProperty("h")
    private String facebookImageUrl;
}