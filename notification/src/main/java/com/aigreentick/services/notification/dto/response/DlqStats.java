
package com.aigreentick.services.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DlqStats {
    private long totalUnprocessed;
    private long totalProcessed;
    private long total;
}