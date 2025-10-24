package com.aigreentick.services.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aigreentick.services.storage.model.FileMetadata;



public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
}
