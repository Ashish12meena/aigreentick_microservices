package com.aigreentick.services.common.model.base;


import java.time.LocalDateTime;

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

    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
