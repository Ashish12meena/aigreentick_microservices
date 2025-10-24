package com.aigreentick.services.template.controller.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aigreentick.services.common.dto.response.ResponseMessage;
import com.aigreentick.services.common.dto.response.ResponseStatus;
import com.aigreentick.services.template.constants.TemplateConstants;
import com.aigreentick.services.template.dto.response.FullTemplateDto;
import com.aigreentick.services.template.service.interfaces.TemplateServiceInterface;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(TemplateConstants.Paths.BASE)
public class TemplateController {
        private final TemplateServiceInterface templateService;

        @GetMapping(TemplateConstants.Paths.TEMPLATE_BY_ID)
        public ResponseEntity<?> getTemplate(@PathVariable String id) {
                FullTemplateDto response = templateService.getTemplateById(id);

                return ResponseEntity.ok(
                                new ResponseMessage<>(
                                                ResponseStatus.SUCCESS.name(),
                                                TemplateConstants.Messages.TEMPLATES_FETCHED,
                                                response));

        }
        

}
