package com.example.aigreentick.services.messaging.message.model.content.parameters;

import java.util.List;

import com.example.aigreentick.services.messaging.message.dto.interfaces.MediaParameter;
import com.example.aigreentick.services.messaging.message.dto.interfaces.TextParameter;
import com.example.aigreentick.services.messaging.message.model.content.media.Document;
import com.example.aigreentick.services.messaging.message.model.content.media.Image;
import com.example.aigreentick.services.messaging.message.model.content.media.Video;
import com.example.aigreentick.services.messaging.message.model.content.template.LimitedTimeOffer;

import lombok.Data;

@Data
public class Parameter implements TextParameter ,MediaParameter {
    private String type;
    private String text;
    private List<TapTargetConfiguration> tapTargetConfiguration;
    private Action action;
    private String couponCode;
    private Image image;
    private Document document;
    private Video video;
    private LimitedTimeOffer limitedTimeOffer;
    private String payload;
    private Product product;
}

