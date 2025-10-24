package com.aigreentick.services.template.dto.resumable;




import lombok.Data;

@Data
public class ResumableMediaUploadRequestDto {
    private String wabaAppId;
    private String accessToken;
    private String apiVersion="v23.0";
}
