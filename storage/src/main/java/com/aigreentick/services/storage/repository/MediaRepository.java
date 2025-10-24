package com.aigreentick.services.storage.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aigreentick.services.storage.enums.MediaType;
import com.aigreentick.services.storage.model.Media;



@Repository
public interface MediaRepository extends JpaRepository<Media, Long>, MediaRepositoryCustom {
    @Query("SELECT m.mediaId FROM Media m WHERE m.id = :id")
    String findMediaIdById(@Param("id") Long id);

    Optional<Media> findByStoredFilename(String storedFilename);

    Optional<Media> findByMediaId(String mediaId);

    Page<Media> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Media> findByUserIdAndMediaTypeOrderByCreatedAtDesc(Long userId, MediaType mediaType, Pageable pageable);

    boolean existsByStoredFilename(String storedFilename);

    long countByUserId(Long userId);

    long countByUserIdAndMediaType(Long userId, MediaType mediaType);
}