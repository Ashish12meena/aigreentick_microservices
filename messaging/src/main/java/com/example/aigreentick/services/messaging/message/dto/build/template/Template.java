package com.example.aigreentick.services.messaging.message.dto.build.template;

import java.util.List;


import lombok.Data;

@Data
public class Template {
    private String id;
    private String name;
    private String category;
    private String language;
    private String status;
    private String metaTemplateId;
    private List<TemplateComponent> components;
    private List<TemplateText> texts;
    
}