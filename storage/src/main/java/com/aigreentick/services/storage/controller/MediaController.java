package com.aigreentick.services.storage.controller;

import java.time.Duration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aigreentick.services.storage.constants.MediaConstants;
import com.aigreentick.services.storage.dto.response.MediaUploadResponse;
import com.aigreentick.services.storage.dto.response.UserMediaResponse;
import com.aigreentick.services.storage.service.impl.media.MediaOrchestratorServiceImpl;
import com.aigreentick.services.storage.validator.MediaRequestValidator;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;

import com.aigreentick.services.common.dto.response.ResponseMessage;
import com.aigreentick.services.common.dto.response.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(MediaConstants.Paths.BASE)
@RequiredArgsConstructor
public class MediaController {
        private final MediaOrchestratorServiceImpl mediaService;
        private final MediaRequestValidator validator;

        /**
         * Uploads a media file.
         *
         * @param file the multipart file to upload
         * @return ResponseEntity with upload details
         */
        @PostMapping(value = MediaConstants.Paths.UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Upload media file", description = "Uploads a media file to both local storage and WhatsApp/Facebook. Supports images, videos, documents, and audio files.")
        public ResponseEntity<ResponseMessage<MediaUploadResponse>> uploadMedia(
                        @RequestParam("file") MultipartFile file) {

                log.info("Upload request received for: {}", file.getOriginalFilename());
                MediaUploadResponse response = mediaService.uploadMedia(file);
                return ResponseEntity.ok(new ResponseMessage<>(ResponseStatus.SUCCESS.name(),
                                MediaConstants.Messages.MEDIA_UPLOADED_SUCCESS, response));
        }

        /**
         * Retrieves all media for the current user with pagination.
         *
         * @param page the page number (0-indexed)
         * @param size the page size
         * @return Page of user media
         */
        @GetMapping
        @Operation(summary = "Get all media", description = "Retrieves all media files for the current user with pagination support")
        public ResponseEntity<ResponseMessage<Page<UserMediaResponse>>> getAllMedia(
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer size) {

                validator.validateUserContext();
                Pageable pageable = validator.validateAndBuildPageable(page, size);

                log.info("Fetching all media for user - page: {}, size: {}", page, size);
                Page<UserMediaResponse> mediaPage = mediaService.getUserMedia(pageable);

                return ResponseEntity.ok(new ResponseMessage<>(ResponseStatus.SUCCESS.name(),
                                MediaConstants.Messages.MEDIA_UPLOADED_SUCCESS, mediaPage));
        }

        /**
         * Retrieves images for the current user with pagination.
         *
         * @param page the page number (0-indexed)
         * @param size the page size
         * @return Page of user images
         */
        @GetMapping("/images")
        @Operation(summary = "Get user images", description = "Retrieves all image files for the current user")
        public ResponseEntity<ResponseMessage<Page<UserMediaResponse>>> getImages(
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer size) {

                validator.validateUserContext();
                Pageable pageable = validator.validateAndBuildPageable(page, size);

                log.info("Fetching images for user - page: {}, size: {}", page, size);
                Page<UserMediaResponse> mediaPage = mediaService.getUserImages(pageable);

                return ResponseEntity.ok(new ResponseMessage<>(ResponseStatus.SUCCESS.name(),
                                MediaConstants.Messages.MEDIA_UPLOADED_SUCCESS, mediaPage));
        }

        /**
         * Retrieves videos for the current user with pagination.
         *
         * @param page the page number (0-indexed)
         * @param size the page size
         * @return Page of user videos
         */
        @GetMapping("/videos")
        @Operation(summary = "Get user videos", description = "Retrieves all video files for the current user")
        public ResponseEntity<ResponseMessage<Page<UserMediaResponse>>> getVideos(
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer size) {

                validator.validateUserContext();
                Pageable pageable = validator.validateAndBuildPageable(page, size);

                log.info("Fetching videos for user - page: {}, size: {}", page, size);
                Page<UserMediaResponse> mediaPage = mediaService.getUserVideos(pageable);

                return ResponseEntity.ok(new ResponseMessage<>(ResponseStatus.SUCCESS.name(),
                                MediaConstants.Messages.MEDIA_UPLOADED_SUCCESS, mediaPage));
        }

        /**
         * Retrieves documents for the current user with pagination.
         *
         * @param page the page number (0-indexed)
         * @param size the page size
         * @return Page of user documents
         */
        @GetMapping("/documents")
        @Operation(summary = "Get user documents", description = "Retrieves all document files for the current user")
        public ResponseEntity<ResponseMessage<Page<UserMediaResponse>>> getDocuments(
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer size) {

                validator.validateUserContext();
                Pageable pageable = validator.validateAndBuildPageable(page, size);

                log.info("Fetching documents for user - page: {}, size: {}", page, size);
                Page<UserMediaResponse> mediaPage = mediaService.getUserDocuments(pageable);

                return ResponseEntity.ok(new ResponseMessage<>(ResponseStatus.SUCCESS.name(),
                                MediaConstants.Messages.MEDIA_UPLOADED_SUCCESS, mediaPage));
        }

        /**
         * Retrieves audio files for the current user with pagination.
         *
         * @param page the page number (0-indexed)
         * @param size the page size
         * @return Page of user audio files
         */
        @GetMapping("/audio")
        @Operation(summary = "Get user audio files", description = "Retrieves all audio files for the current user")
        public ResponseEntity<ResponseMessage<Page<UserMediaResponse>>> getAudio(
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer size) {

                validator.validateUserContext();
                Pageable pageable = validator.validateAndBuildPageable(page, size);

                log.info("Fetching audio files for user - page: {}, size: {}", page, size);
                Page<UserMediaResponse> mediaPage = mediaService.getUserAudio(pageable);

                return ResponseEntity.ok(new ResponseMessage<>(ResponseStatus.SUCCESS.name(),
                                MediaConstants.Messages.MEDIA_UPLOADED_SUCCESS, mediaPage));
        }

        @GetMapping("/public-url")
        @Operation(summary = "Get public URL for media", description = "Generates a temporary public URL for accessing media by storage key")
        public ResponseEntity<ResponseMessage<String>> getPublicUrl(
                        @RequestParam @NotBlank(message = "Storage key is required") String storageKey,
                        @RequestParam(required = false, defaultValue = "3600") Long durationSeconds) {

                validator.validateUserContext();

                log.info("Generating public URL for storage key: {} with duration: {} seconds", storageKey,
                                durationSeconds);

                Duration duration = Duration.ofSeconds(durationSeconds);
                String publicUrl = mediaService.getPublicUrl(storageKey, duration);

                return ResponseEntity.ok(new ResponseMessage<>(
                                ResponseStatus.SUCCESS.name(),
                                "Public URL generated successfully",
                                publicUrl));
        }

}
