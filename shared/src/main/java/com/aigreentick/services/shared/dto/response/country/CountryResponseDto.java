package com.aigreentick.services.shared.dto.response.country;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CountryResponseDto {
    private Long id;
    private String name;
    private String mobileCode;
}

