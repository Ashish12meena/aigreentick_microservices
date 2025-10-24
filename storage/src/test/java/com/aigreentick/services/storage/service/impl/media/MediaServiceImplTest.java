package com.aigreentick.services.storage.service.impl.media;


import com.aigreentick.services.storage.enums.MediaType;
import com.aigreentick.services.storage.exception.MediaNotFoundException;
import com.aigreentick.services.storage.model.Media;
import com.aigreentick.services.storage.repository.MediaRepository;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


/**
 * Unit tests for MediaServiceImpl.
 */
@SpringBootTest // ✅ ONLY THIS
@AutoConfigureMockMvc // ✅ To use MockMvc
@ActiveProfiles("test")
@Slf4j
public class MediaServiceImplTest {
    @Mock
    private MediaRepository mediaRepository;

    private MediaServiceImpl mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaServiceImpl(mediaRepository);
    }


      @Test
    void findByStoredFilename_WhenMediaExists_ShouldReturnMedia() {
        // Given
        String filename = "test-file.jpg";
        Media expectedMedia = createTestMedia();
        when(mediaRepository.findByStoredFilename(filename)).thenReturn(Optional.of(expectedMedia));

        // When
        Optional<Media> result = mediaService.findByStoredFilename(filename);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStoredFilename()).isEqualTo(expectedMedia.getStoredFilename());
        verify(mediaRepository).findByStoredFilename(filename);
    }


     // Helper method to create test media
    private Media createTestMedia() {
        return Media.builder()
            .originalFilename("test.jpg")
            .storedFilename("abc-123.jpg")
            .fileSize(1024L)
            .mimeType("image/jpeg")
            .mediaId("fb-123")
            .mediaUrl("http://localhost/media/abc-123.jpg")
            .mediaType(MediaType.IMAGE)
            .userId(1L)
            .organisationId(1L)
            .status("ACTIVE")
            .isDeleted(false)
            .build();
    }

    @Test
    void findByMediaId_WhenMediaExists_ShouldReturnMedia() {
        // Given
        String mediaId = "123456";
        Media expectedMedia = createTestMedia();
        when(mediaRepository.findByMediaId(mediaId)).thenReturn(Optional.of(expectedMedia));

        // When
        Optional<Media> result = mediaService.findByMediaId(mediaId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getMediaId()).isEqualTo(expectedMedia.getMediaId());
        verify(mediaRepository).findByMediaId(mediaId);
    }


        @Test
    void findByUserId_ShouldReturnPagedMedia() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Media> mediaList = List.of(createTestMedia(), createTestMedia());
        Page<Media> expectedPage = new PageImpl<>(mediaList, pageable, mediaList.size());
        
        when(mediaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
            .thenReturn(expectedPage);

        // When
        Page<Media> result = mediaService.findByUserId(userId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(mediaRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

        @Test
    void findByUserIdAndMediaType_ShouldReturnPagedMediaOfType() {
        // Given
        Long userId = 1L;
        MediaType mediaType = MediaType.IMAGE;
        Pageable pageable = PageRequest.of(0, 10);
        List<Media> mediaList = List.of(createTestMedia());
        Page<Media> expectedPage = new PageImpl<>(mediaList, pageable, mediaList.size());
        
        when(mediaRepository.findByUserIdAndMediaTypeOrderByCreatedAtDesc(userId, mediaType, pageable))
            .thenReturn(expectedPage);

        // When
        Page<Media> result = mediaService.findByUserIdAndMediaType(userId, mediaType, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(mediaRepository).findByUserIdAndMediaTypeOrderByCreatedAtDesc(userId, mediaType, pageable);
    }

     @Test
    void deleteById_WhenMediaExists_ShouldDeleteMedia() {
        // Given
        Long mediaId = 1L;
        when(mediaRepository.existsById(mediaId)).thenReturn(true);

        // When
        mediaService.deleteById(mediaId);

        // Then
        verify(mediaRepository).existsById(mediaId);
        verify(mediaRepository).deleteById(mediaId);    
    }


     @Test
    void deleteById_WhenMediaDoesNotExist_ShouldThrowException() {
        // Given
        Long mediaId = 999L;
        when(mediaRepository.existsById(mediaId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> mediaService.deleteById(mediaId))
            .isInstanceOf(MediaNotFoundException.class)
            .hasMessageContaining("not found");
        
        verify(mediaRepository).existsById(mediaId);
        verify(mediaRepository, never()).deleteById(anyLong());
    }

    @Test
    void softDelete_WhenMediaExists_ShouldSoftDeleteMedia() {
        // Given
        Long mediaId = 1L;
        Long deletedBy = 10L;
        Media media = createTestMedia();
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));
        when(mediaRepository.save(any(Media.class))).thenReturn(media);

        // When
        mediaService.softDeleteById(mediaId, deletedBy);

        // Then
        assertThat(media.isDeleted()).isTrue();
        assertThat(media.getUpdatedByUserId()).isEqualTo(deletedBy);
        assertThat(media.getDeletedAt()).isNotNull();
        verify(mediaRepository).findById(mediaId);
        verify(mediaRepository).save(media);
    }

    @Test
    void softDelete_WhenMediaDoesNotExist_ShouldThrowException() {
        // Given
        Long mediaId = 999L;
        Long deletedBy = 10L;
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> mediaService.softDeleteById(mediaId, deletedBy))
            .isInstanceOf(MediaNotFoundException.class);
        
        verify(mediaRepository).findById(mediaId);
        verify(mediaRepository, never()).save(any(Media.class));
    }

     @Test
    void existsByStoredFilename_WhenMediaExists_ShouldReturnTrue() {
        // Given
        String filename = "test.jpg";
        when(mediaRepository.existsByStoredFilename(filename)).thenReturn(true);

        // When
        boolean result = mediaService.existsByStoredFilename(filename);

        // Then
        assertThat(result).isTrue();
        verify(mediaRepository).existsByStoredFilename(filename);
    }

    @Test
    void countByUserId_ShouldReturnCount() {
        // Given
        Long userId = 1L;
        when(mediaRepository.countByUserId(userId)).thenReturn(5L);

        // When
        long result = mediaService.countByUserId(userId);

        // Then
        assertThat(result).isEqualTo(5L);
        verify(mediaRepository).countByUserId(userId);
    }



}
