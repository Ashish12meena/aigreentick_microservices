package com.aigreentick.services.common.model.base;


import java.time.Instant;

import org.springframework.data.annotation.Id;

import com.aigreentick.services.common.model.feature.Auditable;
import com.aigreentick.services.common.model.feature.SoftDeletable;

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
public abstract class MongoBaseEntity extends BaseEntity<String>
        implements Auditable, SoftDeletable  {

    @Id
    private String id;

    private Instant createdAt;
    private Instant updatedAt;
    private Long createdByUserId;
    private Long updatedByUserId;

    private boolean isDeleted;
    private Instant deletedAt;

    public void markDeleted() {
        this.isDeleted = true;
        this.deletedAt = Instant.now();
    }

    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    public void preUpdate() {
        updatedAt = Instant.now();
    }
}