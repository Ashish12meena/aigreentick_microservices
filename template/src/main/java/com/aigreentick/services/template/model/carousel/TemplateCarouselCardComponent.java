package com.aigreentick.services.template.model.carousel;

import java.util.List;

import com.aigreentick.services.template.enums.MediaFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateCarouselCardComponent {
    private String type;
    private MediaFormat format; 
    private TemplateCarouselExample example;
    private String text;
    private List<TemplateCarouselButton> buttons;
}
