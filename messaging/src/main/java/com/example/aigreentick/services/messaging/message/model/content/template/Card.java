package com.example.aigreentick.services.messaging.message.model.content.template;


import java.util.List;

import lombok.Data;

@Data
public class Card {
    private Integer cardIndex;
    private List<CarouselComponent> components;
}
