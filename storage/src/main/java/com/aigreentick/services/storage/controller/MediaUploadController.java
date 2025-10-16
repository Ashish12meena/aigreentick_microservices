package com.aigreentick.services.storage.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aigreentick.services.storage.constants.MediaConstants;
import com.aigreentick.services.storage.service.impl.upload.MediaUploadServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for media management operations.
 */
@Slf4j
@RestController
@RequestMapping(MediaConstants.Paths.BASE)
@RequiredArgsConstructor
public class MediaUploadController {
    private final MediaUploadServiceImpl mediaService;

    /**
     * Retrieves a media file by filename.
     *
     * @param filename the name of the file to retrieve
     * @return ResponseEntity with the file resource
     */
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getMedia(@PathVariable String filename) {

        log.info("Get media request for: {}", filename);
        Resource resource = mediaService.getMedia(filename);

        // Determine content type
        MediaType contentType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
