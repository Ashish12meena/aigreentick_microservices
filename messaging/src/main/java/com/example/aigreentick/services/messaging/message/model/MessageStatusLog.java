package com.example.aigreentick.services.messaging.message.model;


import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.aigreentick.services.messaging.message.enums.MessageStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "message_status_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatusLog {

    @Id
    private String id;

    @Indexed
    private String messageId;

    @Indexed
    private Long userId;

    private MessageStatus status; // SENT, DELIVERED, SEEN

    private Instant statusUpdatedAt;
}

