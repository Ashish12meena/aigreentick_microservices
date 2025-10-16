package com.aigreentick.services.shared.dto.response.blacklist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistResponseDto {
    // private String email;
    private String mobile;
    private Long countryId;
    private String reason;
    private boolean isBlocked;
    private String expiresAt;
    
}