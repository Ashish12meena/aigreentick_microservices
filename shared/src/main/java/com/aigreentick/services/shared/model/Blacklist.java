package com.aigreentick.services.shared.model;

import java.time.LocalDateTime;

import com.aigreentick.services.common.model.base.JpaBaseEntity;
import com.aigreentick.services.shared.enums.blacklist.BlacklistType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "blacklists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor 
public class Blacklist extends JpaBaseEntity  {

    @Column(length = 20)
    private String mobile;

    @Column(name = "country_id")
    private Long countryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BlacklistType type;

    @Column(name = "sender_id", nullable = true)
    private Long senderId;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = true;

    @Column(name = "reason")
    private String reason;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    

}

