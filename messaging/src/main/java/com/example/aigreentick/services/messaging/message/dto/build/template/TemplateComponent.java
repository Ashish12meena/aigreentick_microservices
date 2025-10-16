package com.example.aigreentick.services.messaging.message.dto.build.template;

import java.util.List;


import lombok.Data;

@Data
public class TemplateComponent {
    private String type;
    private String format;
    private String text;
    private String imageUrl;
    private String mediaUrl;
    private Boolean addSecurityRecommendation;
    private Integer codeExpirationMinutes;
    private List<TemplateComponentButton> buttons;
    private List<TemplateComponentCards> cards;
    private TemplateExample example;
}
