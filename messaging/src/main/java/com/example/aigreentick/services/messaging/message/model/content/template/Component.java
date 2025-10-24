package com.example.aigreentick.services.messaging.message.model.content.template;

import java.util.List;

import com.example.aigreentick.services.messaging.message.model.content.parameters.Parameter;

import lombok.Data;

@Data
public class Component {
    private String type;
    private List<Parameter> parameters;
    private String subType;
    private String index;
    List<Card> cards;

}
