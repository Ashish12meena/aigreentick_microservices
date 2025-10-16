package com.aigreentick.services.storage.controller;

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

}
