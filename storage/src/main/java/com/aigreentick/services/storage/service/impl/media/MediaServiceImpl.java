package com.aigreentick.services.storage.service.impl.media;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aigreentick.services.storage.enums.MediaType;
import com.aigreentick.services.storage.exception.MediaNotFoundException;
import com.aigreentick.services.storage.model.Media;
import com.aigreentick.services.storage.repository.MediaRepository;


import com.aigreentick.services.common.service.base.jpa.JpaBaseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaServiceImpl extends JpaBaseService<Media, Long> {
    private final MediaRepository mediaRepository;

    /**
     * Finds media by stored filename.
     */
    @Transactional(readOnly = true)
    public Optional<Media> findByStoredFilename(String storedFilename) {
        log.debug("Finding media by stored filename: {}", storedFilename);
        return mediaRepository.findByStoredFilename(storedFilename);
    }

    /**
     * Finds media by media ID (WhatsApp/Facebook media ID).
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "media", key = "'mediaId:' + #mediaId", unless = "#result == null")
    public Optional<Media> findByMediaId(String mediaId) {
        log.debug("Finding media by media ID: {}", mediaId);
        return mediaRepository.findByMediaId(mediaId);
    }

    @Transactional(readOnly = true)
    public Page<Media> findByUserId(Long userId, Pageable pageable) {
        log.info("Fetching media for user ID: {} with pagination", userId);
        return mediaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Retrieves media for a specific user filtered by media type with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Media> findByUserIdAndMediaType(Long userId, MediaType mediaType, Pageable pageable) {
        log.info("Fetching {} media for user ID: {} with pagination", mediaType, userId);
        return mediaRepository.findByUserIdAndMediaTypeOrderByCreatedAtDesc(userId, mediaType, pageable);
    }

    /**
     * Deletes a media entity by ID.
     */
    @Transactional
    @CacheEvict(value = { "media", "userMediaList" }, allEntries = true)
    public void deleteById(Long id) {
        log.info("Deleting media entity by ID: {}", id);
        if (!mediaRepository.existsById(id)) {
            throw new MediaNotFoundException("Media not found with ID: " + id);
        }
        mediaRepository.deleteById(id);
    }

    @Transactional
    public void deleteByStoredFilename(String storedFilename) {
        log.info("Deleting media entity by stored filename: {}", storedFilename);
        Media media = mediaRepository.findByStoredFilename(storedFilename)
                .orElseThrow(() -> new MediaNotFoundException("Media not found: " + storedFilename));
        mediaRepository.delete(media);
    }

    @Transactional
    public void softDeleteById(Long mediaId, Long deletedBy) {
        if (mediaRepository.softDeleteById(mediaId, deletedBy) == 0) {
            throw new MediaNotFoundException("Media not found: " + mediaId);
        }
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return mediaRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByStoredFilename(String storedFilename) {
        return mediaRepository.existsByStoredFilename(storedFilename);
    }

    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        return mediaRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long countByUserIdAndMediaType(Long userId, MediaType mediaType) {
        return mediaRepository.countByUserIdAndMediaType(userId, mediaType);
    }

    @Override
    protected JpaRepository<Media, Long> getRepository() {
        return mediaRepository;
    }
}
