package com.aigreentick.services.shared.dto.request.blacklist;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistRequestDto {
    @NotNull
    private String mobile;
    private Long countryId;
    private String reason;
    private String expiresAt;
}
