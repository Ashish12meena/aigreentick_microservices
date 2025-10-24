package com.aigreentick.services.template.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aigreentick.services.common.service.base.mongo.MongoBaseService;
import com.aigreentick.services.template.constants.TemplateConstants;
import com.aigreentick.services.template.dto.response.FullTemplateDto;
import com.aigreentick.services.template.dto.response.MetaTemplateIdOnly;
import com.aigreentick.services.template.enums.TemplateStatus;
import com.aigreentick.services.template.exception.TemplateAlreadyExistsException;
import com.aigreentick.services.template.exception.TemplateNotFoundException;
import com.aigreentick.services.template.model.Template;
import com.aigreentick.services.template.repository.TemplateRepository;
import com.aigreentick.services.template.service.interfaces.TemplateServiceInterface;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateServiceImpl extends MongoBaseService<Template, String> implements TemplateServiceInterface {

    private final TemplateRepository templateRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public FullTemplateDto getTemplateById(String id) {
        log.debug("Fetching template by ID: {}", id);
        return templateRepository.findFullTemplateById(id)
                .orElseThrow(() -> {
                    log.error("Template not found with ID: {}", id);
                    return new IllegalArgumentException(
                            String.format(TemplateConstants.Messages.TEMPLATE_NOT_FOUND, id));
                });
    }

    @Override
    public Template findById(String templateId) {
        log.debug("Finding template entity by ID: {}", templateId);
        return templateRepository.findById(templateId)
                .orElseThrow(() -> {
                    log.error("Template not found with ID: {}", templateId);
                    return new TemplateNotFoundException(
                            String.format(TemplateConstants.Messages.TEMPLATE_NOT_FOUND, templateId));
                });
    }

    @Override
    public boolean existsById(String templateId) {
        log.debug("Checking existence of template by ID: {}", templateId);
        if (templateId == null) {
            log.error("Null templateId passed to existsById()");
            throw new IllegalArgumentException(TemplateConstants.Messages.TEMPLATE_ID_NULL);
        }
        return templateRepository.existsById(templateId);
    }

    @Override
    public List<MetaTemplateIdOnly> findMetaTemplateIdsByUserId(long userId) {
        log.debug("Fetching metaTemplateIds for userId: {}", userId);
        return templateRepository.findMetaTemplateIdsByUserId(userId);
    }

    @Override
    public void deleteByMetaTemplateIdInAndUserId(Set<String> deleteIds, long userId) {
        log.info("Deleting templates with metaTemplateIds: {} for userId: {}", deleteIds, userId);
        templateRepository.deleteByMetaTemplateIdInAndUserId(deleteIds, userId);
    }

    void checkDuplicateTemplate(String name) {
        log.debug("Checking for duplicate template with name: {}", name);
        if (templateRepository.findByName(name).isPresent()) {
            log.error("Duplicate template found with name: {}", name);
            throw new TemplateAlreadyExistsException(
                    String.format(TemplateConstants.Messages.TEMPLATE_EXISTS_MSG, name));
        }
    }

    @Override
    public <T> T findById(String templateId, Class<T> type) {
        log.debug("Finding template by ID: {} with projection type: {}", templateId, type.getSimpleName());
        return templateRepository.findById(templateId, type)
                .orElseThrow(() -> {
                    log.error("Template not found with ID: {}", templateId);
                    return new TemplateNotFoundException(
                            String.format(TemplateConstants.Messages.TEMPLATE_NOT_FOUND, templateId));
                });
    }

    @Override
    public Optional<Template> findByIdAndUserId(String templateId, Long userId) {
        log.debug("Finding template by ID: {} and userId: {}", templateId, userId);
        return templateRepository.findByIdAndUserId(templateId, userId);
    }

    @Override
    @Transactional
    public void updateTemplateStatus(String id, String status) {
        TemplateStatus enumStatus = TemplateStatus.valueOf(status.toUpperCase());

        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update()
                .set("status", enumStatus)
                .set("updated_at", LocalDateTime.now());

        mongoTemplate.updateFirst(query, update, Template.class);
    }

    @Override
    protected MongoRepository<Template, String> getRepository() {
        return templateRepository;
    }
}
