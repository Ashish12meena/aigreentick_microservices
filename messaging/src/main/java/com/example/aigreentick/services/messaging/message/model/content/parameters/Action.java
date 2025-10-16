package com.example.aigreentick.services.messaging.message.model.content.parameters;


import java.util.List;

import com.example.aigreentick.services.messaging.message.model.content.interactive.Section;

import lombok.Data;

@Data
public class Action {
    private String thumbnailProductRetailerId;
    private List<Section> sections;
}
