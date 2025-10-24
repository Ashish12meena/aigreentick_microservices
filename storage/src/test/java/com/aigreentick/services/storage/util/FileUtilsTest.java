package com.aigreentick.services.storage.util;

import com.aigreentick.services.storage.dto.file.FileDetailsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FileUtils.
 */
@ExtendWith(MockitoExtension.class)
class FileUtilsTest {

    @Mock
    private MultipartFile multipartFile;

    @TempDir
    Path tempDir;

    @Test
    void getFileDetails_WhenFileIsValid_ShouldReturnDetails() throws IOException {
        // Given
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(testFile.toPath(), "test content");

        // When
        FileDetailsDto result = FileUtils.getFileDetails(testFile);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFileName()).isEqualTo("test.txt");
        assertThat(result.getFileSize()).isGreaterThan(0);
        assertThat(result.getMimeType()).isNotNull();
    }

    @Test
    void getFileDetails_WhenFileIsNull_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> FileUtils.getFileDetails(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("null");
    }

    @Test
    void getFileDetails_WhenFileDoesNotExist_ShouldThrowException() {
        // Given
        File nonExistentFile = new File("non-existent.txt");

        // When & Then
        assertThatThrownBy(() -> FileUtils.getFileDetails(nonExistentFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not exist");
    }

    @Test
    void convertMultipartToFile_WhenMultipartIsValid_ShouldConvertSuccessfully() throws IOException {
        // Given
        byte[] content = "test content".getBytes();
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        doAnswer(invocation -> {
            File file = invocation.getArgument(0);
            Files.write(file.toPath(), content);
            return null;
        }).when(multipartFile).transferTo(any(File.class));

        // When
        File result = FileUtils.convertMultipartToFile(multipartFile);

        // Then
        assertThat(result).exists();
        assertThat(result).isFile();
        assertThat(result.length()).isEqualTo(content.length);
        
        // Cleanup
        result.delete();
    }

    @Test
    void convertMultipartToFile_WhenMultipartIsNull_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> FileUtils.convertMultipartToFile(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("null or empty");
    }

    @Test
    void convertMultipartToFile_WhenMultipartIsEmpty_ShouldThrowException() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> FileUtils.convertMultipartToFile(multipartFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("null or empty");
    }

    @Test
    void deleteQuietly_WhenFileExists_ShouldDeleteFile() throws IOException {
        // Given
        File testFile = tempDir.resolve("test-delete.txt").toFile();
        Files.writeString(testFile.toPath(), "test");
        assertThat(testFile).exists();

        // When
        FileUtils.deleteQuietly(testFile);

        // Then
        assertThat(testFile).doesNotExist();
    }

    @Test
    void deleteQuietly_WhenFileIsNull_ShouldNotThrowException() {
        // When & Then
        assertThatCode(() -> FileUtils.deleteQuietly(null))
            .doesNotThrowAnyException();
    }

    @Test
    void deleteQuietly_WhenFileDoesNotExist_ShouldNotThrowException() {
        // Given
        File nonExistentFile = new File("non-existent-file.txt");

        // When & Then
        assertThatCode(() -> FileUtils.deleteQuietly(nonExistentFile))
            .doesNotThrowAnyException();
    }

    @Test
    void sanitizeFilename_WhenFilenameHasPathTraversal_ShouldRemoveIt() {
        // When
        String result = FileUtils.sanitizeFilename("../../../etc/passwd");

        // Then
        assertThat(result).doesNotContain("../");
        assertThat(result).doesNotContain("..\\");
    }

    @Test
    void sanitizeFilename_WhenFilenameHasInvalidCharacters_ShouldReplaceWithUnderscore() {
        // When
        String result = FileUtils.sanitizeFilename("test<file>name?.txt");

        // Then
        assertThat(result).doesNotContain("<", ">", "?");
        assertThat(result).contains("_");
    }

    @Test
    void sanitizeFilename_WhenFilenameIsNull_ShouldReturnDefault() {
        // When
        String result = FileUtils.sanitizeFilename(null);

        // Then
        assertThat(result).isEqualTo("file");
    }

    @Test
    void sanitizeFilename_WhenFilenameIsValid_ShouldReturnUnchanged() {
        // Given
        String validFilename = "test-file_123.txt";

        // When
        String result = FileUtils.sanitizeFilename(validFilename);

        // Then
        assertThat(result).isEqualTo(validFilename);
    }

    @Test
    void isPathSafe_WhenPathIsWithinBase_ShouldReturnTrue() throws IOException {
        // Given
        Path basePath = tempDir;
        Path filePath = tempDir.resolve("subdir/file.txt");

        // When
        boolean result = FileUtils.isPathSafe(basePath, filePath);

        // Then
        assertThat(result).isTrue();
    }
}