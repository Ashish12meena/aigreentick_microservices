package com.example.aigreentick.services.messaging.message.model.content;

import java.util.List;

import com.example.aigreentick.services.messaging.message.model.content.contacts.Contacts;
import com.example.aigreentick.services.messaging.message.model.content.media.Audio;
import com.example.aigreentick.services.messaging.message.model.content.media.Document;
import com.example.aigreentick.services.messaging.message.model.content.media.Image;
import com.example.aigreentick.services.messaging.message.model.content.media.Video;
import com.example.aigreentick.services.messaging.message.model.content.template.SendableTemplate;

import lombok.Data;

@Data
public class Content {
    private Text text;
    private Image image;
    private Video video;
    private Audio audio;
    private Document document;
    private Sticker sticker;
    private Location location;
    private List<Contacts> contacts;
    private Interactive interactive;

    private SendableTemplate template;

    public Content(SendableTemplate template){
        this.template = template;
    }
    public Content(){
    }
}
