package com.aigreentick.services.template.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aigreentick.services.common.context.UserContext;
import com.aigreentick.services.common.util.JsonUtils;
import com.aigreentick.services.template.client.dto.response.AccessTokenCredentials;
import com.aigreentick.services.common.dto.response.FacebookApiResponse;
import com.aigreentick.services.template.client.dto.response.UserResponseDto;
import com.aigreentick.services.template.client.impl.UserClientImpl;
import com.aigreentick.services.template.client.impl.WhatsappClientImpl;
import com.aigreentick.services.template.dto.TemplateCreateResponseDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.BaseTemplateRequestDto;
import com.aigreentick.services.template.dto.response.CreateTemplateResponseDto;
import com.aigreentick.services.template.dto.response.MetaTemplateIdOnly;
import com.aigreentick.services.template.dto.response.TemplateResponseDto;
import com.aigreentick.services.template.dto.response.TemplateSyncStats;
import com.aigreentick.services.template.enums.DeleteResult;
import com.aigreentick.services.template.enums.TemplateStatus;
import com.aigreentick.services.template.exception.WhatsAppApiException;
import com.aigreentick.services.template.mapper.TemplateMapper;
import com.aigreentick.services.template.model.Template;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrator service responsible for managing WhatsApp message templates.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateOrchestratorServiceImpl {
    private final WhatsappClientImpl whatsAppTemplateService;
    private final TemplateServiceImpl templateServiceImpl;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private final TemplateMapper templateMapper;
    private final UserClientImpl userService;

    @Value("${pagination.page-size}")
    private int defaultPageSize;

    /**
     * Create Template and save currently not approved by facebook.
     */
    @Transactional
    public TemplateCreateResponseDto createTemplate(CreateTemplateResponseDto createTemplateResponseDto) {
        log.info("user-id is {}: and org-id is {}",UserContext.getUserId(),UserContext.getOrganisationId());

        // Extract the template details and check if not duplicate
        BaseTemplateRequestDto baseTemplateRequestDto = createTemplateResponseDto.getTemplate();
        templateServiceImpl.checkDuplicateTemplate(baseTemplateRequestDto.getName());

        // Fetch WABA credentials
        AccessTokenCredentials accessTokenCredentials = userService.getWabaAccessToken();

        // Serialize the template request to JSON
        String jsonRequest = JsonUtils.serializeToString(baseTemplateRequestDto);

        // Call WhatsApp API
        FacebookApiResponse<JsonNode> fbResponse = whatsAppTemplateService.createTemplate(
                jsonRequest, accessTokenCredentials.getId(), accessTokenCredentials.getAccessToken());

        if (!fbResponse.isSuccess()) {
            log.warn("Failed to create template for user {}: {}", UserContext.getUserId(), fbResponse.getErrorMessage());
            return new TemplateCreateResponseDto(false, null, null, null,
                    fbResponse.getErrorMessage(), null);
        }

        JsonNode jsonData = fbResponse.getData();

        // Handle Facebook-specific errors
        if (jsonData.has("error")) {
            String errorMessage = jsonData.path("error").path("message").asText("Unknown error");
            return new TemplateCreateResponseDto(false, null, null, null, errorMessage, jsonData);
        }

        String templateId = jsonData.path("id").asText(null);
        String status = jsonData.path("status").asText(null);
        String category = jsonData.path("category").asText(null);

        if (templateId == null || status == null) {
            return new TemplateCreateResponseDto(false, null, null, null,
                    "Invalid response from Facebook API", jsonData);
        }

        String data = JsonUtils.serializeToString(jsonData);


        // Map the request and response to Template entity
        Template template = templateMapper.toTemplateEntity(
                UserContext.getUserId(),
                jsonRequest,
                baseTemplateRequestDto,
                data,
                category,
                createTemplateResponseDto.getVariables());

        template.setMetaTemplateId(templateId);
        template.setStatus(TemplateStatus.valueOf(status.toUpperCase()));

        // Step 14: Save the template to the database
        templateServiceImpl.save(template);

        // Step 15: Build and return a successful response DTO
        return new TemplateCreateResponseDto(true, templateId, status, category, null, null);
    }

    @Transactional
    public TemplateSyncStats syncTemplatesWithFacebook(long userId) {

        // 1. Fetch WABA access token (from UserService or config)
        AccessTokenCredentials accessTokenCredentials = userService.getWabaAccessToken();

        // 2. Call Facebook API to get all templates
        FacebookApiResponse<JsonNode> response = whatsAppTemplateService.getAllTemplates(
                accessTokenCredentials.getId(),
                accessTokenCredentials.getAccessToken(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty());

        if (response.getStatusCode() != 200 || response.getData() == null) {
            throw new IllegalStateException("Failed to fetch templates from Facebook");
        }

        // Convert Facebook API response into DTO list
        List<BaseTemplateRequestDto> facebookTemplates = convertToBaseTemplateRequestDtoList(response.getData());

        //  Extract Facebook template IDs only
        Set<String> facebookTemplateIds = facebookTemplates.stream()
                .map(BaseTemplateRequestDto::getId)
                .collect(Collectors.toSet());

        //  Fetch existing template IDs from DB for this user
        List<String> metaTemplateIds = templateServiceImpl.findMetaTemplateIdsByUserId(userId)
                .stream()
                .map(MetaTemplateIdOnly::getMetaTemplateId)
                .toList();

        Set<String> setOfMetaTemplateIds = new HashSet<>(metaTemplateIds);

        //  Determine new templates to insert (in FB but not in DB)
        Set<String> newIds = new HashSet<>(facebookTemplateIds);
        newIds.removeAll(setOfMetaTemplateIds);

        //  Determine stale templates to delete (in DB but not in FB)
        Set<String> deleteIds = new HashSet<>(setOfMetaTemplateIds);
        deleteIds.removeAll(facebookTemplateIds);

        //  Prepare entities for insertion
        List<Template> toInsert = facebookTemplates.stream()
                .filter(dto -> newIds.contains(dto.getId()))
                .map(dto -> templateMapper.toTemplateEntity(dto, userId))
                .toList();

        //  Delete stale templates
        if (!deleteIds.isEmpty()) {
            templateServiceImpl.deleteByMetaTemplateIdInAndUserId(deleteIds, userId);
        }

        //  Insert new templates
        if (!toInsert.isEmpty()) {
            templateServiceImpl.saveAll(toInsert);
        }

        // Return statistics
        return new TemplateSyncStats(toInsert.size(), deleteIds.size());
    }

    public TemplateResponseDto updateTemplateStatus(String templateId, Long id) {

        //  Fetch template from DB
        TemplateResponseDto templateDetails = templateServiceImpl.findById(templateId, TemplateResponseDto.class);

        // 2. Skip API call if already approved
        if (TemplateStatus.APPROVED.getValue().equals(templateDetails.getStatus())) {
            return templateDetails;
        }

        // 3. Fetch credentials like wabaId and accessToken
        AccessTokenCredentials credentials = userService.getWabaAccessToken();

        // 4. Fetch latest status from WhatsApp API
        FacebookApiResponse<JsonNode> whatsappResponse = whatsAppTemplateService.getTemplateByName(
                templateDetails.getName(),
                credentials.getId(),
                credentials.getAccessToken());

        if (whatsappResponse == null || !whatsappResponse.isSuccess()) {
            throw new WhatsAppApiException("Failed to fetch template status from WhatsApp API");
        }

        JsonNode dataNode = whatsappResponse.getData();
        if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
            throw new WhatsAppApiException("Template not found on WhatsApp for name: " + templateDetails.getName());
        }

        TemplateStatus latestStatus = TemplateStatus.fromValue(dataNode.get(0).path("status").asText());

        //  Update in DB if status has changed
        if (!latestStatus.getValue().equals(templateDetails.getStatus())) {
            templateServiceImpl.updateTemplateStatus(templateId, latestStatus.getValue());
            log.info("Updated template [{}] status from [{}] to [{}]", templateId, templateDetails.getStatus(), latestStatus);
        }

        // Return updated DTO
        return new TemplateResponseDto(
                templateDetails.getId(),
                templateDetails.getName(),
                latestStatus.getValue(),
                templateDetails.getCategory(),
                templateDetails.getLanguage(),
                templateDetails.getMetaTemplateId());

    }

    /**
     * Retrieves paginated templates for a given user with optional filtering by
     * status and search term.
     */
    public Page<TemplateResponseDto> getTemplatesForUser(
            Long userId,
            String status,
            String search,
            Integer page,
            Integer pageSize) {

        // Ensure user exists
        UserResponseDto user = userService.findById(userId);

        // Default pagination size if not provided
        int sizeOfPage = (pageSize != null && pageSize > 0)
                ? pageSize
                : defaultPageSize;

        Pageable pageable = PageRequest.of(page, sizeOfPage, Sort.by(Sort.Direction.DESC, "id"));

        TemplateStatus templateStatus = validateTemplateStatus(status);

        // 3 Build query criteria
        Criteria criteria = Criteria.where("userId").is(user.getId());
        if (templateStatus != null) {
            criteria = criteria.and("status").is(templateStatus);
        }
        if (search != null && !search.isBlank()) {
            criteria = criteria.and("name").regex(search, "i"); // case-insensitive
        }

        Query query = new Query(criteria).with(pageable);

        // 4. Fetch total count for proper pagination metadata
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Template.class);

        // 5. Fetch actual page content
        List<Template> templates = mongoTemplate.find(query, Template.class);

        // 6. Convert to DTOs for Response
        List<TemplateResponseDto> dtoList = templates.stream()
                .map(templateMapper::toTemplateDto)
                .toList();

        return new PageImpl<>(dtoList, pageable, total);
    }

    // Softly Delete Template
    public DeleteResult SoftDeleteTemplateByIdAndUser(String templateId, Long userId) {
        return templateServiceImpl.findByIdAndUserId(templateId, userId)
                .map(template -> {
                    if (template.getDeletedAt() != null) {
                        return DeleteResult.ALREADY_DELETED;
                    }
                    template.setDeletedAt(LocalDateTime.now());
                    template.setDeleted(true);
                    templateServiceImpl.save(template);
                    return DeleteResult.SUCCESS;
                })
                .orElse(DeleteResult.NOT_FOUND);
    }
    

    private List<BaseTemplateRequestDto> convertToBaseTemplateRequestDtoList(JsonNode responseNode) {
        JsonNode dataNode = responseNode.get("data");
        List<BaseTemplateRequestDto> dtos;
        try {
            dtos = objectMapper
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .readValue(
                            dataNode.traverse(),
                            new TypeReference<List<BaseTemplateRequestDto>>() {
                            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to map JsonNode list to BaseTemplateRequestDto list", e);
        }
        return dtos;
    }

    private TemplateStatus validateTemplateStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return TemplateStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid template status: " + status);
        }
    }
}
