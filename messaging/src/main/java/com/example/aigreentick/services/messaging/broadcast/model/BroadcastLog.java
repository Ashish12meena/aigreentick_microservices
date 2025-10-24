package com.example.aigreentick.services.messaging.broadcast.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "broadcast_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastLog {
    @Id
    private String id; 

    @Indexed
    @Field("broadcast_id")
    private Long broadcastId;

    @Indexed
    @Field("mobile")
    private String mobile;

    @Field("type")
    private String type;

    @Field("message_id")
    private String messageId;

    @Field("wa_id")
    private String waId;

    @Field("message_status")
    private String messageStatus;

    @Field("status")
    private String status; 

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    @Field("deleted_at")
    private Instant deletedAt;

}
