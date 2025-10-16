package com.aigreentick.services.storage.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.aigreentick.services.storage.dto.response.UserMediaResponse;
import com.aigreentick.services.storage.enums.MediaType;
import com.aigreentick.services.storage.model.Media;
import com.aigreentick.services.common.context.UserContext;

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

    public Media toEntity(String url, String originalFilename, String storedFilename, MediaType mediaType,
            String contentType, Long fileSizeBytes, LocalDateTime uploadedAt) {
        return Media.builder()
                .mediaUrl(url)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .mediaType(mediaType)
                .mimeType(contentType)
                .fileSize(fileSizeBytes)
                .userId(UserContext.getUserId())
                .organisationId(UserContext.getOrganisationId())
                .createdAt(uploadedAt)
                .build();
    }
}
