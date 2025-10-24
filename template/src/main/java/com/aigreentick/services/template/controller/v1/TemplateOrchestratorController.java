package com.aigreentick.services.template.controller.v1;


import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aigreentick.services.common.context.UserContext;
import com.aigreentick.services.common.dto.response.ResponseMessage;
import com.aigreentick.services.common.dto.response.ResponseStatus;
import com.aigreentick.services.template.constants.TemplateConstants;
import com.aigreentick.services.template.dto.TempalatePaginationRequestDto;
import com.aigreentick.services.template.dto.TemplateCreateResponseDto;
import com.aigreentick.services.template.dto.response.CreateTemplateResponseDto;
import com.aigreentick.services.template.dto.response.TemplateResponseDto;
import com.aigreentick.services.template.dto.response.TemplateSyncStats;
import com.aigreentick.services.template.service.impl.TemplateOrchestratorServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(TemplateConstants.Paths.BASE)
public class TemplateOrchestratorController {
    private final TemplateOrchestratorServiceImpl templateOrchestratorServiceImpl;

    @PostMapping(TemplateConstants.Paths.CREATE)
    public ResponseEntity<?> createTemplate(@RequestBody @Valid CreateTemplateResponseDto request,HttpServletRequest req) {
        TemplateCreateResponseDto response = templateOrchestratorServiceImpl.createTemplate(request);
        return ResponseEntity
                .ok(new ResponseMessage<>(ResponseStatus.SUCCESS.name(), TemplateConstants.Messages.TEMPLATE_CREATED, response));
    }

    @GetMapping(TemplateConstants.Paths.SYNC_MY_TEMPLATES)
    public ResponseEntity<?> syncTemplateWithFacebook() {
        TemplateSyncStats response = templateOrchestratorServiceImpl
                .syncTemplatesWithFacebook(UserContext.getUserId());
        return ResponseEntity.ok(new ResponseMessage<>(ResponseStatus.SUCCESS.name(), TemplateConstants.Messages.TEMPLATES_FETCHED, response));
    }

    @PatchMapping(TemplateConstants.Paths.UPDATE_STATUS)
    public ResponseEntity<?> updateTemplateStatus(@PathVariable String templateId) {
        TemplateResponseDto response = templateOrchestratorServiceImpl.updateTemplateStatus(templateId,
                UserContext.getUserId());
        return ResponseEntity.ok(new ResponseMessage<>(ResponseStatus.SUCCESS.name(),  TemplateConstants.Messages.TEMPLATES_FETCHED, response));
    }

    /**
     * Get all templates for the authenticated user with optional search and
     * pagination.
     */
    @GetMapping(TemplateConstants.Paths.MY_TEMPLATES)
    public ResponseEntity<?> getUserTemplates(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) String status,
            @Valid TempalatePaginationRequestDto pagination) {
        Page<TemplateResponseDto> templates = templateOrchestratorServiceImpl.getTemplatesForUser(UserContext.getUserId(), status,
                search,
                pagination.getPage(), pagination.getSize());
        return ResponseEntity.ok(new ResponseMessage<>(ResponseStatus.SUCCESS.name(),  TemplateConstants.Messages.TEMPLATES_FETCHED, templates));
    }
}
