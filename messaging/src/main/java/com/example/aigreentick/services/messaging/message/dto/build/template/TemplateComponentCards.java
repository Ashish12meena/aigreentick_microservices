package com.example.aigreentick.services.messaging.message.dto.build.template;

import java.util.List;

import lombok.Data;

@Data
public class TemplateComponentCards {
    private Integer index;
    private List<TemplateCarouselCardComponent> components;
}
