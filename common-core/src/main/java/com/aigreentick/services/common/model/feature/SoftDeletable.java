package com.aigreentick.services.common.model.feature;

import java.time.Instant;

public interface SoftDeletable {
    boolean isDeleted();
    Instant getDeletedAt();
    void markDeleted();
}

