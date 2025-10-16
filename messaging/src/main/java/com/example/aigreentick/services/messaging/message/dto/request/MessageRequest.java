package com.example.aigreentick.services.messaging.message.dto.request;

import java.lang.reflect.AccessFlag.Location;
import java.util.List;

import com.example.aigreentick.services.messaging.message.enums.MessageType;
import com.example.aigreentick.services.messaging.message.model.content.Reaction;
import com.example.aigreentick.services.messaging.message.model.content.Sticker;
import com.example.aigreentick.services.messaging.message.model.content.contacts.Contacts;
import com.example.aigreentick.services.messaging.message.model.content.interactive.InteractiveDto;
import com.example.aigreentick.services.messaging.message.model.content.media.Audio;
import com.example.aigreentick.services.messaging.message.model.content.media.Document;
import com.example.aigreentick.services.messaging.message.model.content.media.Image;
import com.example.aigreentick.services.messaging.message.model.content.template.SendableTemplate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequest {

    private String messagingProduct = "whatsapp";
    private String recipientType = "individual";

    @NotNull
    private String to;

    @NotNull
    private MessageType type;

    private InteractiveDto interactive;

    private Audio audio;

    private List<Contacts> contacts;

    private Document document;

    private Image image;

    private Location location;

    private Reaction  reaction;

    private Sticker sticker;

    private SendableTemplate template;


}
