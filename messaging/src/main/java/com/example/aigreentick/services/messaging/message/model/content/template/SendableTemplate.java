package com.example.aigreentick.services.messaging.message.model.content.template;



import java.util.List;

import com.example.aigreentick.services.messaging.message.model.content.parameters.Language;

import lombok.Data;

@Data
public class SendableTemplate {
    private String name;
    private Language language;
    List<Component> components;
}
    