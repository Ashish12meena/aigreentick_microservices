package com.aigreentick.services.template.dto.resumable;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UploadSessionResponse {
    @JsonProperty("id")
    private String uploadSessionId;
}
