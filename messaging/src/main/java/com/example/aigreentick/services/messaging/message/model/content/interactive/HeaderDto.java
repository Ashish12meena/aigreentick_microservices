package com.example.aigreentick.services.messaging.message.model.content.interactive;

import com.example.aigreentick.services.messaging.message.model.content.Text;
import com.example.aigreentick.services.messaging.message.model.content.media.Document;
import com.example.aigreentick.services.messaging.message.model.content.media.Image;

import lombok.Data;

@Data
public class HeaderDto {
    private String type;
    private Document document;
    private Image image;
    private Text text;
}