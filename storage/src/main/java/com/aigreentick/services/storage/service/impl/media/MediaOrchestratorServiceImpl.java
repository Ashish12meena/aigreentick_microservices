package com.aigreentick.services.storage.service.impl.media;

import com.aigreentick.services.storage.client.dto.response.AccessTokenCredentials;
import com.aigreentick.services.storage.client.dto.response.WhatsappMediaUploadResponseDto;
import com.aigreentick.services.storage.client.service.impl.UserClientAdapter;
import com.aigreentick.services.storage.client.service.impl.WhatsappClientAdapter;
import com.aigreentick.services.storage.dto.response.MediaUploadResponse;
import com.aigreentick.services.storage.dto.response.UserMediaResponse;
import com.aigreentick.services.storage.dto.storage.StorageMetadata;
import com.aigreentick.services.storage.dto.storage.StorageResult;
import com.aigreentick.services.storage.enums.MediaType;
import com.aigreentick.services.storage.exception.MediaUploadException;
import com.aigreentick.services.storage.exception.MediaValidationException;
import com.aigreentick.services.storage.mapper.MediaMapper;
import com.aigreentick.services.storage.model.Media;
import com.aigreentick.services.storage.service.StorageProvider;
import com.aigreentick.services.storage.util.FileUtils;
import com.aigreentick.services.storage.validator.ClientValidator;
import com.aigreentick.services.storage.validator.MediaValidator;
import com.aigreentick.services.common.context.UserContext;
import com.aigreentick.services.common.dto.response.FacebookApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaOrchestratorServiceImpl {
    
    private final StorageProvider storageProvider;
    private final WhatsappClientAdapter whatsappClient;
    private final MediaMapper mediaMapper;
    private final UserClientAdapter userClient;
    private final MediaServiceImpl mediaService;
    private final ClientValidator clientValidator;
    private final MediaValidator mediaValidator;

    /**
     * Orchestrates media upload to storage provider and Facebook/WhatsApp.
     */
    @Transactional
    public MediaUploadResponse uploadMedia(MultipartFile multipart) {
        validateMultipartFile(multipart);

        File tempFile = null;

        try {
            // Validate file size against quota
            clientValidator.validateStorageInfo(multipart.getSize());

            // Determine media type
            String contentType = multipart.getContentType();
            MediaType mediaType = mediaValidator.detectMediaType(contentType);

            // Build storage metadata
            StorageMetadata metadata = StorageMetadata.builder()
                    .originalFilename(multipart.getOriginalFilename())
                    .contentType(contentType)
                    .fileSize(multipart.getSize())
                    .userId(UserContext.getUserId())
                    .organisationId(UserContext.getOrganisationId())
                    .mediaType(mediaType)
                    .fileExtension(extractFileExtension(multipart.getOriginalFilename()))
                    .build();

            // Upload to storage provider (S3/Local/etc)
            log.info("Uploading media to storage provider: {}", storageProvider.getProviderType());
            StorageResult storageResult = storageProvider.save(multipart.getInputStream(), metadata);

            // Create Media entity
            Media media = mediaMapper.toEntity(
                    storageResult,
                    multipart.getOriginalFilename(),
                    mediaType,
                    contentType,
                    multipart.getSize(),
                    LocalDateTime.now()
            );

            // Optional: Upload to Facebook/WhatsApp
            String whatsappMediaId = null;
            try {
                tempFile = FileUtils.convertMultipartToFile(multipart);
                MediaUploadResponse whatsappResponse = uploadToFacebook(tempFile, metadata);
                if (whatsappResponse != null && whatsappResponse.getMediaId() != null) {
                    whatsappMediaId = whatsappResponse.getMediaId();
                    media.setMediaId(whatsappMediaId);
                }
            } catch (Exception e) {
                log.warn("Failed to upload to WhatsApp, continuing with storage only: {}", e.getMessage());
            }

            // Save to database
            mediaService.save(media);

            log.info("Media upload completed successfully: storageKey={}, provider={}", 
                     storageResult.getStorageKey(), 
                     storageResult.getProvider());

            return MediaUploadResponse.builder()
                    .url(storageResult.getPublicUrl())
                    .originalFilename(multipart.getOriginalFilename())
                    .storedFilename(storageResult.getStorageKey())
                    .mediaType(mediaType)
                    .contentType(contentType)
                    .mediaId(whatsappMediaId)
                    .fileSizeBytes(multipart.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload media: {}", e.getMessage(), e);
            throw new MediaUploadException("Media upload failed: " + e.getMessage(), e);
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    public String getPublicUrl(String storageKey, Duration duration){
        return storageProvider.getPublicUrl(storageKey, duration);
    }

    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserMedia(Pageable pageable) {
        Long userId = UserContext.getUserId();
        log.info("Fetching all media for user ID: {}", userId);

        Page<Media> mediaPage = mediaService.findByUserId(userId, pageable);
        return mediaPage.map(mediaMapper::toUserMediaResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserMediaByType(MediaType mediaType, Pageable pageable) {
        Long userId = UserContext.getUserId();
        log.info("Fetching {} media for user ID: {}", mediaType, userId);

        Page<Media> mediaPage = mediaService.findByUserIdAndMediaType(userId, mediaType, pageable);
        return mediaPage.map(mediaMapper::toUserMediaResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserImages(Pageable pageable) {
        return getUserMediaByType(MediaType.IMAGE, pageable);
    }

    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserVideos(Pageable pageable) {
        return getUserMediaByType(MediaType.VIDEO, pageable);
    }

    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserDocuments(Pageable pageable) {
        return getUserMediaByType(MediaType.DOCUMENT, pageable);
    }

    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserAudio(Pageable pageable) {
        return getUserMediaByType(MediaType.AUDIO, pageable);
    }

    // Helper methods

    private MediaUploadResponse uploadToFacebook(File file, StorageMetadata metadata) {
        AccessTokenCredentials accessTokenCredentials = userClient
                .getPhoneNumberIdAccessToken(UserContext.getUserId());

        FacebookApiResponse<WhatsappMediaUploadResponseDto> response = whatsappClient
                .uploadMediaToFacebook(
                        file,
                        metadata.getContentType(),
                        accessTokenCredentials.getId(),
                        accessTokenCredentials.getAccessToken()
                );

        if (!response.isSuccess()) {
            throw new MediaUploadException(response.getErrorMessage(), response.getStatusCode());
        }

        String mediaId = response.getData().getId();

        return MediaUploadResponse.builder()
                .mediaId(mediaId)
                .build();
    }

    private void validateMultipartFile(MultipartFile multipart) {
        if (multipart == null || multipart.isEmpty()) {
            log.error("Upload failed: file is empty or null");
            throw new MediaValidationException("Uploaded file is empty or null");
        }
    }

    private String extractFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}