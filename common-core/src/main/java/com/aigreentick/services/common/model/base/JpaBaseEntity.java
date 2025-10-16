package com.aigreentick.services.common.model.base;

import java.time.LocalDateTime;

import com.aigreentick.services.common.model.feature.Auditable;
import com.aigreentick.services.common.model.feature.SoftDeletable;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class JpaBaseEntity extends BaseEntity<Long>
        implements Auditable, SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long createdByUserId;

    private Long updatedByUserId;

    private boolean isDeleted;

    private LocalDateTime deletedAt;

    public void markDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}