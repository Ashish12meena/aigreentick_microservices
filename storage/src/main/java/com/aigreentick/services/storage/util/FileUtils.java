package com.aigreentick.services.storage.util;



import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.web.multipart.MultipartFile;

import com.aigreentick.services.storage.dto.file.FileDetailsDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {

    public static FileDetailsDto getFileDetails(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("The provided File is null, does not exist, or is not a valid file.");
        }
        String filename = file.getName();
        long fileSize = file.length();

        // Guess MIME type from file name (fallback if unknown)
        String mimeType;
        try {
            mimeType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            log.warn("Could not determine MIME type for file {}. Defaulting to application/octet-stream", filename);
            mimeType = "application/octet-stream";
        }

        return FileDetailsDto.builder()
                .fileName(filename)
                .mimeType(mimeType)
                .fileSize(fileSize)
                .build();
    }

    public static File toTemFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("MultipartFile is null or empty.");
        }

        try {
            // Create a temp file with the original name preserved
            String originalFilename = multipartFile.getOriginalFilename();
            String prefix = originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "upload";
            File tempFile = File.createTempFile(prefix, null);

            // Transfer content to the file
            multipartFile.transferTo(tempFile);

            log.debug("MultipartFile successfully converted to File: {}", tempFile.getAbsolutePath());
            return tempFile;
        } catch (Exception ex) {
            log.error("Failed to convert MultipartFile to File: {}", ex.getMessage(), ex);
            throw new RuntimeException("File conversion failed", ex);
        }
    }

    public static File convertMultipartToFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("MultipartFile is null or empty.");
        }

        try {
            // Extract original filename
            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "upload";
            }
            
            // Sanitize filename: remove spaces and invalid characters
            originalFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");

            // Split prefix and suffix (extension)
            String prefix;
            String suffix;
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
                prefix = originalFilename.substring(0, dotIndex);
                suffix = originalFilename.substring(dotIndex); // includes dot
            } else {
                prefix = originalFilename;
                suffix = null; // no extension
            }

            // Ensure prefix is at least 3 chars for createTempFile
            if (prefix.length() < 3) {
                prefix = prefix + "___";
            } else if (prefix.length() > 50) {
                prefix = prefix.substring(0, 50); // trim to avoid OS errors
            }

            // Create temp file with proper extension
            File tempFile = File.createTempFile(prefix + "_", suffix);
            tempFile.deleteOnExit(); // ensures cleanup on JVM exit

            // Transfer content
            multipartFile.transferTo(tempFile);

            return tempFile;

        } catch (IOException ex) {
            throw new RuntimeException("Failed to convert MultipartFile to File", ex);
        }
    }

    public static void deleteQuietly(File f) {
        if (f != null)
            try {
                Files.deleteIfExists(f.toPath());
            } catch (IOException ignore) {
            }
    }
}
