package com.example.aigreentick.services.messaging.flow.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "flow_submission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowSubmission {

    public enum Status {
        RECEIVED, PROCESSED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wa_id")
    private String waId;

    @Column(name = "flow_token")
    private String flowToken;

    @Column(name = "flow_id")
    private String flowId;

    @Column(name = "screen")
    private String screen;

    @Column(name = "action")
    private String action;

    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    @Lob
    @Column(name = "raw_encrypted_json")
    private String rawEncryptedJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;
}
