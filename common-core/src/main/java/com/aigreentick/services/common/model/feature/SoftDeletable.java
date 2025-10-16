package com.aigreentick.services.common.model.feature;

import java.time.LocalDateTime;

public interface SoftDeletable {
    boolean isDeleted();
    LocalDateTime getDeletedAt();
    void markDeleted();
}

