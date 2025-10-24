package com.aigreentick.services.storage.model;

import com.aigreentick.services.common.model.base.JpaBaseEntity;
import com.aigreentick.services.storage.enums.MediaType;
import com.aigreentick.services.storage.enums.StorageProviderType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Media extends JpaBaseEntity  {

    private String originalFilename;

    private String storedFilename;

    private Long fileSize;

    private String mimeType;

    private String  mediaId;

    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType; // IMAGE, VIDEO, DOCUMENT, AUDIO

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider")
    private StorageProviderType storageProvider;

    @Column(name = "storage_bucket")
    private String storageBucket;
    
    @Column(name = "storage_key")
    private String storageKey;
    
    @Column(name = "storage_region")
    private String storageRegion;

    // Actual path on server or storage
    private String storagePath;

    private String status; // e.g., PENDING, COMPLETED, FAILED

    private Long organisationId;

    private Long userId; // Optional if you need user tracking

    private String wabaId;

}
