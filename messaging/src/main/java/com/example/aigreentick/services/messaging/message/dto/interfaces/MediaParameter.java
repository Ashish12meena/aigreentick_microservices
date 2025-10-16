package com.example.aigreentick.services.messaging.message.dto.interfaces;

import com.example.aigreentick.services.messaging.message.model.content.media.Document;
import com.example.aigreentick.services.messaging.message.model.content.media.Image;
import com.example.aigreentick.services.messaging.message.model.content.media.Video;

public interface MediaParameter {
    void setType(String type);
    void setDocument(Document document);
    void setImage(Image image);
    void setVideo(Video video);
}
