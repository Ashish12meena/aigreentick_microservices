package com.aigreentick.services.storage.client.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UploadSessionResponse {
    @JsonProperty("id")
    private String uploadSessionId;
}
