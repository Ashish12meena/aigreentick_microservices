package com.aigreentick.services.template.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.aigreentick.services.template.dto.response.TemplateResponseDto;
import com.aigreentick.services.template.enums.TemplateStatus;
import com.aigreentick.services.template.mapper.TemplateMapper;
import com.aigreentick.services.template.model.Template;
import com.aigreentick.services.template.service.interfaces.TemplateServiceInterface;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateAdminServiceImpl {
    private final MongoTemplate mongoTemplate;
    private final TemplateMapper templateMapper;
    private final TemplateServiceInterface templateService;

    public Page<TemplateResponseDto> getAdminFilteredTemplates(
            Long userId,
            String status,
            String name,
            String waId,
            String category,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size) {

        List<Criteria> criteriaList = new ArrayList<>();
        if (userId != null) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }

        if (status != null && !status.isBlank()) {
            criteriaList.add(Criteria.where("status").is(TemplateStatus.valueOf(status)));
        }

        if (name != null && !name.isBlank()) {
            criteriaList.add(Criteria.where("name").regex(name, "i")); // case-insensitive search
        }

        if (category != null && !category.isBlank()) {
            criteriaList.add(Criteria.where("category").is(category));
        }

        if (fromDate != null && toDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(fromDate).lte(toDate));
        } else if (fromDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(fromDate));
        } else if (toDate != null) {
            criteriaList.add(Criteria.where("createdAt").lte(toDate));
        }

        // Soft delete check
        criteriaList.add(Criteria.where("isDeleted").is(false));

        Criteria finalCriteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            finalCriteria.andOperator(criteriaList.toArray(new Criteria[0]));
        }

        Query query = new Query(finalCriteria);

        // Sort + pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(pageable);

        long total = mongoTemplate.count(query, Template.class);
        List<Template> templates = mongoTemplate.find(query, Template.class);
        List<TemplateResponseDto> templateResponseDtos = templateMapper.toTemplateDtoList(templates);

        return new PageImpl<>(templateResponseDtos, pageable, total);

    }

    public boolean adminDeleteTemplate(String templateId) {
        Template template = templateService.findById(templateId);

        if (template.getDeletedAt() != null)
            return false; // already deleted

        template.setDeletedAt(LocalDateTime.now());
        templateService.save(template);
        return true;
    }

   

}
