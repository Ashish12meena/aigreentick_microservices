package com.example.aigreentick.services.messaging.broadcast.model;

import java.time.LocalDateTime;
import java.util.List;

import com.aigreentick.services.common.model.base.JpaBaseEntity;
import com.example.aigreentick.services.messaging.broadcast.enums.CampaignStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Campaign extends JpaBaseEntity {

    private Long userId;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "waba_id")
    private String wabaId;

    @Column(name = "country_id", nullable = false)
    private Long countryId;

    @Column(name = "is_media", nullable = false)
    private boolean isMedia = false;

    @Column(name = "total_recipients")
    private Integer totalRecipients = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status = CampaignStatus.PENDING;

    @Column(name = "schedule_at")
    private LocalDateTime scheduleAt;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Broadcast> broadcasts;

}
