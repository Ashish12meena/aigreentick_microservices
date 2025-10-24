package com.aigreentick.services.storage.dto.storage;


import com.aigreentick.services.storage.enums.MediaType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageMetadata {
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private Long userId;
    private Long organisationId;
    private MediaType mediaType;
    private String fileExtension;
    
    /**
     * Generate storage key based on organization/user hierarchy
     */
    public String generateStorageKey() {
        String uuid = java.util.UUID.randomUUID().toString();
        return String.format("org-%d/user-%d/%s/%s%s",
            organisationId,
            userId,
            mediaType.name().toLowerCase(),
            uuid,
            fileExtension != null ? fileExtension : ""
        );
    }
}