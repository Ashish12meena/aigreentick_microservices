package com.aigreentick.services.template.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.aigreentick.services.template.dto.response.FullTemplateDto;
import com.aigreentick.services.template.dto.response.MetaTemplateIdOnly;
import com.aigreentick.services.template.enums.TemplateStatus;
import com.aigreentick.services.template.model.Template;
// import com.aigreentick.services.template.dto.CategoryCountDto;
// import com.aigreentick.services.template.dto.StatusCountDto;

@Repository
public interface TemplateRepository extends MongoRepository<Template, String> {

    // -------------------------------------------------------------------------
    // Basic Retrieval
    // ---------------------------------------------------------------------d:\WhatsappProject\aigreentick_springboot_services\aigreentick_springboot_services\src\main\java\com\aigreentick\services\template\service\impl\TemplateServiceImpl.java----

    <T> Optional<T> findById(String templateId, Class<T> type);

    Optional<Template> findByIdAndUserId(String id, Long userId);

    Optional<Template> findByName(String name);

    List<Template> findAllByUserId(long userId);

    // -------------------------------------------------------------------------
    // Search & Pagination
    // -------------------------------------------------------------------------

    Page<Template> findAll(Pageable pageable);

    Page<Template> findByUserIdAndNameContainingIgnoreCase(Long userId, String search, Pageable pageable);

    Page<Template> findByUserIdAndStatus(Long userId, TemplateStatus status, Pageable pageable);

    Page<Template> findByUserIdAndStatusAndNameContainingIgnoreCase(Long userId, TemplateStatus status,
            String search, Pageable pageable);

    // -------------------------------------------------------------------------
    // Filters
    // -------------------------------------------------------------------------

    List<Template> findByUserId(Long userId);

    List<Template> findByStatus(TemplateStatus status);

    // -------------------------------------------------------------------------
    // Counts
    // -------------------------------------------------------------------------

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, TemplateStatus status);

    long countByStatus(TemplateStatus status);

    long countByDeletedAtIsNull();

    // -------------------------------------------------------------------------
    // Deletion
    // -------------------------------------------------------------------------

    void deleteByMetaTemplateIdInAndUserId(Set<String> deleteIds, long userId);

    // -------------------------------------------------------------------------
    // Projection
    // -------------------------------------------------------------------------

    @Query(value = "{ 'userId': ?0 }", fields = "{ 'metaTemplateId': 1, '_id': 0}")
    List<MetaTemplateIdOnly> findMetaTemplateIdsByUserId(Long userId);

    Optional<FullTemplateDto> findFullTemplateById(String id);

}
