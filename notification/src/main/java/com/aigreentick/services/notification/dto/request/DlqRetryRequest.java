// src/main/java/com/aigreentick/services/notification/dto/request/DlqRetryRequest.java
package com.aigreentick.services.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DlqRetryRequest {
    private String requestedBy;
    private String notes;
}