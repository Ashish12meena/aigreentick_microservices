package com.aigreentick.services.common.model.feature;

import java.time.LocalDateTime;

public interface Auditable {
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    Long getCreatedByUserId();
    Long getUpdatedByUserId();

    void setCreatedAt(LocalDateTime createdAt);
    void setUpdatedAt(LocalDateTime updatedAt);
    void setCreatedByUserId(Long id);
    void setUpdatedByUserId(Long id);
}