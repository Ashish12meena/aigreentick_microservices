package com.aigreentick.services.storage.model;


import com.aigreentick.services.common.model.base.JpaBaseEntity;
import com.aigreentick.services.storage.enums.MediaType;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "media")
@Getter
@Setter
@SuperBuilder
public class Media extends JpaBaseEntity {

    private String originalFilename;

    private String storedFilename;

    private Long fileSize;

    private String mimeType;

    private String mediaId;

    private String mediaUrl;

    private MediaType mediaType; // IMAGE, VIDEO, DOCUMENT, AUDIO

    // Actual path on server or storage
    private String storagePath;

    private String status; // e.g., PENDING, COMPLETED, FAILED

    private Long organisationId;

    private Long userId; // Optional if you need user tracking

    private String wabaId;

}
