package com.aigreentick.services.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.aigreentick.services.common.dto.response.FileMetaData;

public class FileUtils {
    public static FileMetaData getFileDetails(File file) {
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
            mimeType = "application/octet-stream";
        }

        return FileMetaData.builder()
                .fileName(filename)
                .mimeType(mimeType)
                .fileSize(fileSize)
                .build();
    }
}
