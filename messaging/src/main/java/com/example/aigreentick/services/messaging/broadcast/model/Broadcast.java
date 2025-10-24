package com.example.aigreentick.services.messaging.broadcast.model;

import java.time.LocalDateTime;

import com.aigreentick.services.common.model.base.JpaBaseEntity;
import com.example.aigreentick.services.messaging.broadcast.enums.BroadcastStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "broadcasts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Broadcast extends JpaBaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

   @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "organization_id")
    private Long organizationId;

    @JoinColumn(name = "country_id")
    private Long countryId;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "wabaId_id")
    private String wabaId;

    @Column(name = "media_id")
    private Long mediaId;

    private String mediaUrl;

    @Column(name = "is_media")
    private boolean isMedia = false;

    @Column(columnDefinition = "text")
    private String data;

    @Column(name = "total_numbers", nullable = false)
    private Integer totalNumbers = 0;

    @Column(name = "schedule_at")
    private LocalDateTime scheduleAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BroadcastStatus status = BroadcastStatus.SCHEDULED;

    @Lob
    @Column(name = "recipients")
    private String recipients;

    @Lob
    @Column(name = "payload")
    private String payload;

}
