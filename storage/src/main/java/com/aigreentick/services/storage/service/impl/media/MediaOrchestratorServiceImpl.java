package com.aigreentick.services.storage.service.impl.media;

import java.io.File;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aigreentick.services.storage.client.dto.response.AccessTokenCredentials;
import com.aigreentick.services.storage.client.dto.response.WhatsappMediaUploadResponseDto;
import com.aigreentick.services.storage.client.service.impl.UserClientAdapter;
import com.aigreentick.services.storage.client.service.impl.WhatsappClientAdapter;
import com.aigreentick.services.storage.dto.file.FileDetailsDto;
import com.aigreentick.services.storage.dto.response.MediaUploadResponse;
import com.aigreentick.services.storage.dto.response.UserMediaResponse;
import com.aigreentick.services.storage.enums.MediaType;
import com.aigreentick.services.storage.exception.MediaUploadException;
import com.aigreentick.services.storage.exception.MediaValidationException;
import com.aigreentick.services.storage.mapper.MediaMapper;
import com.aigreentick.services.storage.model.Media;
import com.aigreentick.services.storage.service.impl.upload.MediaUploadServiceImpl;
import com.aigreentick.services.storage.util.FileUtils;
import com.aigreentick.services.storage.validator.ClientValidator;
import com.aigreentick.services.storage.validator.MediaValidator;
import com.aigreentick.services.common.context.UserContext;
import com.aigreentick.services.common.dto.response.FacebookApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaOrchestratorServiceImpl {
    private final WhatsappClientAdapter whatsappClient;
    private final MediaMapper mediaMapper;
    private final UserClientAdapter userClient;
    private final MediaUploadServiceImpl mediaUploadService;
    private final MediaServiceImpl mediaService;
    private final ClientValidator clientValidator;
    private final MediaValidator mediaValidator;

    /**
     * Orchestrates media upload to both local server and Facebook/WhatsApp.
     * 
     * @param multipart the uploaded file
     * @return MediaUploadResponse with URLs and media ID
     * @throws MediaValidationException if validation fails
     * @throws MediaUploadException     if upload fails
     */
    @Transactional
    public MediaUploadResponse uploadMedia(MultipartFile multipart) {
        validateMultipartFile(multipart);

        File file = null;

        try {
            // Convert and get file details
            file = FileUtils.convertMultipartToFile(multipart);
            FileDetailsDto details = FileUtils.getFileDetails(file);

            clientValidator.validateStorageInfo(details.getFileSize());

            MediaType mediaType = mediaValidator.detectMediaType(details.getMimeType());
            validateFile(file, mediaType);

            // Upload to local server
            log.info("Uploading media to local server: {}", details.getFileName());
            MediaUploadResponse serverResponse = uploadToLocalServer(multipart);

            Media media = buildMediaEntity(serverResponse);

            // optional write logic if want to upload on facebook
            MediaUploadResponse whatsappResponse = uploadToFacebook(file, details);
            if (whatsappResponse.getMediaId() != null) {
                media.setMediaId(whatsappResponse.getMediaId());
            }

            mediaService.save(media);

            return MediaUploadResponse.builder()
                    .url(serverResponse.getUrl())
                    .originalFilename(details.getFileName())
                    .storedFilename(serverResponse.getStoredFilename())
                    .mediaType(mediaType)
                    .contentType(details.getMimeType())
                    .mediaId(whatsappResponse.getMediaId())
                    .fileSizeBytes(serverResponse.getFileSizeBytes())
                    .uploadedAt(serverResponse.getUploadedAt())
                    .build();
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    /**
     * Retrieves all media for the current user with pagination.
     *
     * @param pageable pagination parameters
     * @return Page of user media
     */
    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserMedia(Pageable pageable) {
        Long userId = UserContext.getUserId();
        log.info("Fetching all media for user ID: {}", userId);

        Page<Media> mediaPage = mediaService.findByUserId(userId, pageable);
        return mediaPage.map(mediaMapper::toUserMediaResponse);
    }

    /**
     * Retrieves media for the current user filtered by media type with pagination.
     *
     * @param mediaType the type of media to filter by
     * @param pageable  pagination parameters
     * @return Page of user media filtered by type
     */
    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserMediaByType(MediaType mediaType, Pageable pageable) {
        Long userId = UserContext.getUserId();
        log.info("Fetching {} media for user ID: {}", mediaType, userId);

        Page<Media> mediaPage = mediaService.findByUserIdAndMediaType(userId, mediaType, pageable);
        return mediaPage.map(mediaMapper::toUserMediaResponse);
    }

    /** 
     * Retrieves images for the current user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserImages(Pageable pageable) {
        return getUserMediaByType(MediaType.IMAGE, pageable);
    }

    /**
     * Retrieves videos for the current user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserVideos(Pageable pageable) {
        return getUserMediaByType(MediaType.VIDEO, pageable);
    }

    /**
     * Retrieves documents for the current user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserDocuments(Pageable pageable) {
        return getUserMediaByType(MediaType.DOCUMENT, pageable);
    }

    /**
     * Retrieves audio files for the current user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<UserMediaResponse> getUserAudio(Pageable pageable) {
        return getUserMediaByType(MediaType.AUDIO, pageable);
    }

    // helper
    private Media buildMediaEntity(MediaUploadResponse response) {
        return mediaMapper.toEntity(
                response.getUrl(),
                response.getOriginalFilename(),
                response.getStoredFilename(),
                response.getMediaType(),
                response.getContentType(),
                response.getFileSizeBytes(),
                response.getUploadedAt());
    }

    private MediaUploadResponse uploadToFacebook(File file, FileDetailsDto details) {

        AccessTokenCredentials accessTokenCredentials = userClient
                .getPhoneNumberIdAccessToken(UserContext.getUserId());

        FacebookApiResponse<WhatsappMediaUploadResponseDto> upMedFecRes = whatsappClient
                .uploadMediaToFacebook(
                        file,
                        details.getMimeType(),
                        accessTokenCredentials.getId(),
                        accessTokenCredentials.getAccessToken());

        if (!upMedFecRes.isSuccess()) {
            throw new MediaUploadException(upMedFecRes.getErrorMessage(), upMedFecRes.getStatusCode());
        }

        String mediaId = upMedFecRes.getData().getId();

        return MediaUploadResponse.builder()
                .mediaId(mediaId)
                .build();

    }

    private void validateFile(File file, MediaType mediaType) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new MediaValidationException("Invalid file: file is null, missing, or not a regular file.");
        }
        long size = file.length();
        if (size > mediaType.getMaxBytes()) {
            throw new MediaValidationException(
                    String.format("File size %d bytes exceeds allowed max for %s (%d bytes).",
                            size, mediaType.getValue(), mediaType.getMaxBytes()));
        }

        long absoluteMax = MediaType.constants.ABSOLUTE_MAX; // 100 MB absolute
        if (size > absoluteMax) {
            throw new MediaValidationException(
                    String.format("File size %d bytes exceeds absolute WhatsApp limit (%d bytes).",
                            size, absoluteMax));
        }
    }

    /**
     * Validates the uploaded multipart file.
     */
    private void validateMultipartFile(MultipartFile multipart) {
        if (multipart == null || multipart.isEmpty()) {
            log.error("Upload failed: file is empty or null");
            throw new MediaValidationException("Uploaded file is empty or null");
        }
    }

    /**
     * Uploads file to local server.
     */
    private MediaUploadResponse uploadToLocalServer(MultipartFile multipart) {
        try {
            return mediaUploadService.uploadMedia(multipart);
        } catch (Exception e) {
            log.error("Failed to upload media to local server: {}", e.getMessage(), e);
            throw new MediaUploadException("Local server upload failed: " + e.getMessage(), e);
        }
    }

}
