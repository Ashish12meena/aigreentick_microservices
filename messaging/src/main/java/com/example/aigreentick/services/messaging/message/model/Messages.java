package com.example.aigreentick.services.messaging.message.model;




import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.aigreentick.services.messaging.message.enums.MessageStatus;
import com.example.aigreentick.services.messaging.message.enums.MessageType;
import com.example.aigreentick.services.messaging.message.model.content.Content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "messages")
public class Messages {
    @Id
    private String id;

    private Long userId;

    private String waId;  // whatsapp Id

    private String phoneNumberId;

    @Indexed
    private Long broadcastId;

    @Indexed
    private Long campaignId;

    private String payload;

    private String response;

    private String from; // from phoneNumberId
     
    private String to; //to mobile numbers

    private MessageType type;  // like location, template etc.

    private String messageId;

    private MessageStatus status; 
    
    private Context context;
    
    private Content content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
