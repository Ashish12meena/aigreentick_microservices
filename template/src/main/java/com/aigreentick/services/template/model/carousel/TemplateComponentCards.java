package com.aigreentick.services.template.model.carousel;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateComponentCards {
    private Integer index;
    private List<TemplateCarouselCardComponent> components;
}
