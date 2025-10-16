package com.aigreentick.services.storage.service.impl.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aigreentick.services.storage.config.MediaServiceProperties;
import com.aigreentick.services.storage.dto.response.MediaUploadResponse;
import com.aigreentick.services.storage.enums.MediaType;
import com.aigreentick.services.storage.exception.MediaDeletionException;
import com.aigreentick.services.storage.exception.MediaNotFoundException;
import com.aigreentick.services.storage.exception.MediaUploadException;
import com.aigreentick.services.storage.validator.MediaValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Production-grade implementation of MediaService.
 * Handles upload, retrieval, and deletion of all media types.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaUploadServiceImpl {
    private final MediaValidator mediaValidator;
    private final MediaServiceProperties properties;

    /**
     * Uploads a media file with validation and error handling.
     *
     * @param file the multipart file to upload
     * @return MediaUploadResponse with file details and URL
     * @throws MediaUploadException if upload fails
     */

    public MediaUploadResponse uploadMedia(MultipartFile file) {
        log.info("Starting media upload for file: {}", file.getOriginalFilename());
        try {
            // Validate the file
            mediaValidator.validateFile(file);

            // Generate unique filename
            String storedFilename = generateUniqueFilename(file.getOriginalFilename());

            // Determine media type
            String contentType = file.getContentType();
            MediaType mediaType = mediaValidator.determineMediaType(contentType);

            // Create subdirectory based on media type
            Path uploadPath = createUploadPath(mediaType);

            // Resolve complete file path
            Path filePath = uploadPath.resolve(storedFilename);

            //  Ensure directory exists
            Files.createDirectories(filePath.getParent());

            //  Copy file to storage
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Successfully uploaded media: {}", storedFilename);

            // Build and return response
            // Build and return response
            return buildUploadResponse(
                    file,
                    storedFilename,
                    mediaType,
                    contentType);
        } catch (IOException e) {
            log.error("Failed to upload media: {}", file.getOriginalFilename(), e);
            throw new MediaUploadException("Failed to store file: " + file.getOriginalFilename(), e);
        } catch (Exception e) {
            log.error("Unexpected error during media upload: {}", file.getOriginalFilename(), e);
            throw new MediaUploadException("Unexpected error during upload: " + e.getMessage());
        }
    }

    /**
     * Retrieves a media file by filename.
     *
     * @param filename the name of the file to retrieve
     * @return Resource containing the file
     * @throws MediaNotFoundException if file doesn't exist
     */
    public Resource getMedia(String filename) {
        log.info("Retrieving media: {}", filename);

        try {
            // Search for file across all media type directories
            Resource resource = findMediaResource(filename);

            if (resource == null || !resource.exists() || !resource.isReadable()) {
                log.warn("Media not found or not readable: {}", filename);
                throw new MediaNotFoundException("Media file not found: " + filename);
            }

            log.info("Successfully retrieved media: {}", filename);
            return resource;

        } catch (MediaNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving media: {}", filename, e);
            throw new MediaUploadException("Failed to retrieve media: " + filename, e);
        }
    }

    /**
     * Deletes a media file by filename.
     *
     * @param filename the name of the file to delete
     * @return true if deletion was successful
     * @throws MediaNotFoundException if file doesn't exist
     */
    public boolean deleteMedia(String filename) {
        log.info("Deleting media: {}", filename);

        try {
            // Find the file path
            Path filePath = findMediaPath(filename);

            if (filePath == null || !Files.exists(filePath)) {
                log.warn("Media not found for deletion: {}", filename);
                throw new MediaNotFoundException("Media file not found: " + filename);
            }

            // Delete the file
            Files.delete(filePath);

            log.info("Successfully deleted media: {}", filename);
            return true;

        } catch (MediaNotFoundException e) {
            throw e;
        } catch (IOException e) {
            log.error("Failed to delete media: {}", filename, e);
            throw new MediaDeletionException("Failed to delete media: " + filename, e);
        }
    }

    /**
     * Checks if a media file exists.
     *
     * @param filename the name of the file to check
     * @return true if file exists
     */
    public boolean mediaExists(String filename) {
        Path filePath = findMediaPath(filename);
        return filePath != null && Files.exists(filePath);
    }

    // ========== Private Helper Methods ==========

    /**
     * Finds the file path for a given filename.
     */
    private Path findMediaPath(String filename) {
        for (MediaType type : MediaType.values()) {
            Path path = createUploadPath(type).resolve(filename).normalize();

            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
    }

    /**
     * Searches for media file across all subdirectories.
     */
    private Resource findMediaResource(String filename) throws IOException {
        for (MediaType type : MediaType.values()) {
            Path path = createUploadPath(type).resolve(filename).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        }
        return null;
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = extractFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Extracts file extension from filename.
     */
    private String extractFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Creates upload path based on media type for better organization.
     */
    private Path createUploadPath(MediaType mediaType) {
        String subDir = mediaType.name().toLowerCase();
        return Paths.get(properties.getUploadPath(), subDir);
    }

    /**
     * Builds the upload response DTO.
     */
    private MediaUploadResponse buildUploadResponse(
            MultipartFile file,
            String storedFilename,
            MediaType mediaType,
            String contentType) {

        String publicUrl = properties.getBaseUrl()  + storedFilename;

        return MediaUploadResponse.builder()
                .url(publicUrl)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .mediaType(mediaType)
                .contentType(contentType)
                .fileSizeBytes(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();
    }
}
