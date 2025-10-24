package com.example.aigreentick.services.messaging.message.model.content.interactive;

import java.util.List;

import lombok.Data;

@Data
public class ActionDto {
    private String name;  // address_message ,send_location, flow
    private ParametersDto parameters;
    private List<Section> sections;
    private List<Button> buttons;
}
