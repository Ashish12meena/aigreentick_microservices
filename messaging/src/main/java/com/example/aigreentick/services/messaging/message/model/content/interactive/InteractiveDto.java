package com.example.aigreentick.services.messaging.message.model.content.interactive;

import lombok.Data;

@Data
public class InteractiveDto {
     private String type;   // address_message , cta_url , flow , list , button,location_request_message
    private BodyDto body;
    private ActionDto action;
    private HeaderDto header;
    private FooterDto footer;
}
