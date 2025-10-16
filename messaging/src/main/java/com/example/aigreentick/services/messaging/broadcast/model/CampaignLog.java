package com.example.aigreentick.services.messaging.broadcast.model;


import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "campaign_logs")
public class CampaignLog {
    @Id
    private String id;

    @Indexed
    @Field("campaign_id")
    private Long campaignId;

    @Indexed
    private String mobile;

    private String messageId;

    private String type = "template";

    private String waId;

    private String messageStatus;

    @NonNull
    @Indexed
    private Status status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant deletedAt;

    public enum Status {
        success, pending, failed
    }

}

