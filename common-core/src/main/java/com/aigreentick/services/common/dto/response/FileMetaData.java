package com.aigreentick.services.common.dto.response;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class FileMetaData {
    private final String fileName;
    private final long fileSize;
    private final String mimeType;

    public FileMetaData(String fileName, long fileSize, String mimeType) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

}

