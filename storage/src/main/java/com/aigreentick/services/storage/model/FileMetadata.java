package com.aigreentick.services.storage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import com.aigreentick.services.common.model.base.JpaBaseEntity;

@Entity
@Getter
@Setter
@Table(name = "file_metadata")
@SuperBuilder
public class FileMetadata extends JpaBaseEntity {

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false, unique = true)
    private String storedFilename;

    private String contentType;

    private long sizeInBytes;

    @Column(length = 1000)
    private String storagePath;

    private String checksum; // MD5 or SHA256

    private Boolean scanned;

    private String scanResult;

    private String uploaderId;

}
