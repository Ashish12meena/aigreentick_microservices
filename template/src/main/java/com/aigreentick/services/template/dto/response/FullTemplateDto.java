package com.aigreentick.services.template.dto.response;

import java.util.List;

import com.aigreentick.services.template.model.TemplateComponent;

public interface FullTemplateDto {
    String getId();
    String getName();
    String getCategory();
    String getLanguage();
    String getStatus();
    String getMetaTemplateId();
    List<TemplateComponent> getComponents();
    
}
