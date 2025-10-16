package com.aigreentick.services.storage.validator;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.aigreentick.services.common.context.UserContext;
import com.aigreentick.services.storage.config.PaginationProperties;
import com.aigreentick.services.storage.exception.MediaValidationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator for HTTP request parameters and context in media operations.
 * Provides centralized validation logic for pagination, user context, and
 * filenames.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaRequestValidator {
    private final PaginationProperties paginationProperties;

    /**
     * Validates and builds a safe Pageable object with limits.
     *
     * @param page the requested page number
     * @param size the requested page size
     * @return validated Pageable object
     */
    public Pageable validateAndBuildPageable(Integer page, Integer size) {
        page = page != null ? page : 0;
        size = size != null ? size : paginationProperties.getDefaultPageSize();

        if (page < 0) {
            log.warn("Invalid page number requested: {}", page);
            throw new MediaValidationException("Page number must be >= 0");
        }

        if (size < paginationProperties.getMinPageSize()) {
            log.warn("Invalid page size requested: {}", size);
            throw new MediaValidationException("Page size must be > " + paginationProperties.getMinPageSize());
        }

        if (size > paginationProperties.getMaxPageSize()) {
            log.warn("Page size {} exceeds maximum {}, clamping to max", size, paginationProperties.getMaxPageSize());
            size = paginationProperties.getMaxPageSize();
        }

        return PageRequest.of(page, size);
    }

    /**
     * Validates that user context is available and valid.
     *
     * @throws MediaValidationException if user context is invalid or missing
     */
    public void validateUserContext() {
        Long userId = UserContext.getUserId();
        Long orgId = UserContext.getOrganisationId();

        if (userId == null || userId <= 0) {
            log.error("Invalid user context - userId: {}", userId);
            throw new MediaValidationException("User context is invalid or missing");
        }

        if (orgId == null || orgId <= 0) {
            log.error("Invalid organisation context - orgId: {}", orgId);
            throw new MediaValidationException("Organisation context is invalid or missing");
        }
    }

    /**
     * Validates filename to prevent path traversal and injection attacks.
     *
     * @param filename the filename to validate
     * @throws MediaValidationException if filename is invalid
     */
    public void validateFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            log.error("Empty or null filename provided");
            throw new MediaValidationException("Filename cannot be empty");
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            log.error("Invalid filename with path traversal attempt: {}", filename);
            throw new MediaValidationException("Invalid filename format");
        }
    }
}
