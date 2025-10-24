package com.aigreentick.services.template.client.dto.response;



import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UploadOffsetResponse {

    private String id;

    @JsonProperty("file_offset")
    private String fileOffset;
}