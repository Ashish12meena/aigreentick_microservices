package com.aigreentick.services.template.service.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.aigreentick.services.template.dto.response.FullTemplateDto;
import com.aigreentick.services.template.dto.response.MetaTemplateIdOnly;
// import com.aigreentick.services.template.dto.response.FullTemplateDto;
// import com.aigreentick.services.template.dto.response.MetaTemplateIdOnly;
import com.aigreentick.services.template.model.Template;

public interface TemplateServiceInterface {

    FullTemplateDto getTemplateById(String id);

    Template findById(String templateId);

    boolean existsById(String templateId);

    List<MetaTemplateIdOnly> findMetaTemplateIdsByUserId(long userId);

    void deleteByMetaTemplateIdInAndUserId(Set<String> deleteIds, long userId);

    List<Template> saveAll(List<Template> toInsert);

    Template save(Template template);

    <T> T findById(String templateId, Class<T> type);

    Optional<Template> findByIdAndUserId(String templateId, Long userId);

    void updateTemplateStatus(String id, String status);

}