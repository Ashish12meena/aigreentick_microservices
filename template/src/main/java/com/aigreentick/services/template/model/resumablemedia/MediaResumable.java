package com.aigreentick.services.template.model.resumablemedia;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;

import com.aigreentick.services.common.model.base.MongoBaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "templates")
@Builder
public class MediaResumable extends MongoBaseEntity {

    private String sessionId; // e.g., upload:123456

    private String mediaHandle; // Final "h" string returned (used to send message)

    private String fileName;

    private Long fileSize;

    private String mimeType;

    private String mediaType; // IMAGE, VIDEO, DOCUMENT, AUDIO

    private String status; // e.g., PENDING, COMPLETED, FAILED

    private Long uploadedByUserId; // Optional if you need user tracking

    private String wabaId;

    private String uploadResponseJson; // full raw JSON response (for debugging)

    private LocalDateTime uploadedAt;

    private LocalDateTime completedAt;

    private Boolean isChunkedUpload; // true if uploaded in parts

    private Long fileOffset; // For resume logic (optional)

}
