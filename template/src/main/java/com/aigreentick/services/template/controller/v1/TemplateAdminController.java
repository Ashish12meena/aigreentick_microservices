package com.aigreentick.services.template.controller.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aigreentick.services.template.config.TemplateServiceProperties;
import com.aigreentick.services.template.constants.TemplateConstants;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(TemplateConstants.Paths.Admin.BASE)
public class TemplateAdminController {
    private final TemplateServiceProperties properties;

    @PostMapping(TemplateConstants.Paths.Admin.TOGGLE_INCOMING)
    public ResponseEntity<String> toggleIncoming(@RequestParam boolean enable) {
        properties.setIncomingEnabled(enable);
        return ResponseEntity.ok("Incoming requests are now " + (enable ? "enabled" : "disabled"));
    }

    @GetMapping(TemplateConstants.Paths.Admin.INCOMING_STATUS)
    public ResponseEntity<String> getIncomingStatus() {
        return ResponseEntity
                .ok("Incoming requests are currently " + (properties.isIncomingEnabled() ? "enabled" : "disabled"));
    }
}
