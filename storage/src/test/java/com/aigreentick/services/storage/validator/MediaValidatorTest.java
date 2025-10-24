package com.aigreentick.services.storage.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.aigreentick.services.storage.config.MediaServiceProperties;
import com.aigreentick.services.storage.enums.MediaType;
import com.aigreentick.services.storage.exception.InvalidMediaException;

/**
 * Unit tests for MediaValidator.
 */
@ExtendWith(MockitoExtension.class)
public class MediaValidatorTest {
    @Mock
    private MediaServiceProperties properties;

    @Mock
    private MultipartFile multipartFile;

    private MediaValidator mediaValidator;


    @BeforeEach
    void setUp() {
        mediaValidator = new MediaValidator(properties);
        
        // Setup default properties
        when(properties.getUploadMaxSize()).thenReturn(52428800L); // 50MB
        when(properties.getUploadAllowedImageTypes()).thenReturn(List.of("image/jpeg", "image/png"));
        when(properties.getUploadAllowedVideoTypes()).thenReturn(List.of("video/mp4"));
        when(properties.getUploadAllowedDocumentTypes()).thenReturn(List.of("application/pdf"));
        when(properties.getUploadAllowedAudioTypes()).thenReturn(List.of("audio/mpeg"));
    }

     @Test
    void validateFile_WhenFileIsNull_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> mediaValidator.validateFile(null))
            .isInstanceOf(InvalidMediaException.class)
            .hasMessageContaining("empty or null");
    }

     @Test
    void validateFile_WhenFileIsEmpty_ShouldThrowException() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> mediaValidator.validateFile(multipartFile))
            .isInstanceOf(InvalidMediaException.class)
            .hasMessageContaining("empty or null");
    }


     @Test
    void validateFile_WhenFileSizeExceedsLimit_ShouldThrowException() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(100000000L); // 100MB
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");

        // When & Then
        assertThatThrownBy(() -> mediaValidator.validateFile(multipartFile))
            .isInstanceOf(InvalidMediaException.class)
            .hasMessageContaining("exceeds maximum");
    }


    @Test
    void validateFile_WhenContentTypeIsNull_ShouldThrowException() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn(null);
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");

        // When & Then
        assertThatThrownBy(() -> mediaValidator.validateFile(multipartFile))
            .isInstanceOf(InvalidMediaException.class)
            .hasMessageContaining("Content type is missing");
    }


        @Test
    void validateFile_WhenContentTypeNotAllowed_ShouldThrowException() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("application/exe");
        when(multipartFile.getOriginalFilename()).thenReturn("test.exe");

        // When & Then
        assertThatThrownBy(() -> mediaValidator.validateFile(multipartFile))
            .isInstanceOf(InvalidMediaException.class)
            .hasMessageContaining("not allowed");
    }

     @Test
    void validateFile_WhenFilenameHasPathTraversal_ShouldThrowException() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getOriginalFilename()).thenReturn("../../../etc/passwd");

        // When & Then
        assertThatThrownBy(() -> mediaValidator.validateFile(multipartFile))
            .isInstanceOf(InvalidMediaException.class)
            .hasMessageContaining("path traversal");
    }

    @Test
    void validateFile_WhenFilenameHasNoExtension_ShouldThrowException() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getOriginalFilename()).thenReturn("testfile");

        // When & Then
        assertThatThrownBy(() -> mediaValidator.validateFile(multipartFile))
            .isInstanceOf(InvalidMediaException.class)
            .hasMessageContaining("extension");
    }

    @Test
    void validateFile_WhenFileIsValid_ShouldNotThrowException() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");

        // When & Then
        assertThatCode(() -> mediaValidator.validateFile(multipartFile))
            .doesNotThrowAnyException();
    }

    @Test
    void determineMediaType_ForImageContentType_ShouldReturnImage() {
        // When
        MediaType result = mediaValidator.determineMediaType("image/jpeg");

        // Then
        assertThat(result).isEqualTo(MediaType.IMAGE);
    }

    @Test
    void determineMediaType_ForVideoContentType_ShouldReturnVideo() {
        // When
        MediaType result = mediaValidator.determineMediaType("video/mp4");

        // Then
        assertThat(result).isEqualTo(MediaType.VIDEO);
    }

    @Test
    void determineMediaType_ForAudioContentType_ShouldReturnAudio() {
        // When
        MediaType result = mediaValidator.determineMediaType("audio/mpeg");

        // Then
        assertThat(result).isEqualTo(MediaType.AUDIO);
    }

    @Test
    void determineMediaType_ForDocumentContentType_ShouldReturnDocument() {
        // When
        MediaType result = mediaValidator.determineMediaType("application/pdf");

        // Then
        assertThat(result).isEqualTo(MediaType.DOCUMENT);
    }

    @Test
    void determineMediaType_ForUnsupportedContentType_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> mediaValidator.determineMediaType("application/exe"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported media type");
    }

    @Test
    void detectMediaType_ForValidMimeType_ShouldReturnCorrectType() {
        // When
        MediaType result = mediaValidator.detectMediaType("image/png");

        // Then
        assertThat(result).isEqualTo(MediaType.IMAGE);
    }

    @Test
    void detectMediaType_ForNullMimeType_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> mediaValidator.detectMediaType(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Content type is missing");
    }

}
