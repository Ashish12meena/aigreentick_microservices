package com.aigreentick.services.storage.mapper;

import com.aigreentick.services.storage.dto.response.UserMediaResponse;
import com.aigreentick.services.storage.dto.storage.StorageResult;
import com.aigreentick.services.storage.enums.MediaType;
import com.aigreentick.services.storage.model.Media;
import com.aigreentick.services.common.context.UserContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MediaMapper {

    /**
     * Converts Media entity to UserMediaResponse DTO.
     */
    public UserMediaResponse toUserMediaResponse(Media media) {
        if (media == null) {
            return null;
        }

        return UserMediaResponse.builder()
                .id(media.getId())
                .url(media.getMediaUrl())
                .originalFilename(media.getOriginalFilename())
                .storedFilename(media.getStoredFilename())
                .mediaType(media.getMediaType())
                .contentType(media.getMimeType())
                .mediaId(media.getMediaId())
                .fileSizeBytes(media.getFileSize())
                .uploadedAt(media.getCreatedAt())
                .build();
    }

    /**
     * Create Media entity from StorageResult and metadata
     */
    public Media toEntity(StorageResult storageResult, String originalFilename, 
                         MediaType mediaType, String contentType, 
                         Long fileSizeBytes, LocalDateTime uploadedAt) {
        return Media.builder()
                .mediaUrl(storageResult.getPublicUrl())
                .originalFilename(originalFilename)
                .storedFilename(storageResult.getStorageKey())
                .mediaType(mediaType)
                .mimeType(contentType)
                .fileSize(fileSizeBytes)
                // NEW: Storage provider fields
                .storageProvider(storageResult.getProvider())
                .storageBucket(storageResult.getBucket())
                .storageKey(storageResult.getStorageKey())
                .storageRegion(storageResult.getRegion())
                // User context
                .userId(UserContext.getUserId())
                .organisationId(UserContext.getOrganisationId())
                .createdAt(uploadedAt)
                .build();
    }
}