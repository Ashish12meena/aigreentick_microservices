package com.aigreentick.services.shared.mapper;

import org.springframework.stereotype.Component;

import com.aigreentick.services.shared.dto.response.country.CountryResponseDto;
import com.aigreentick.services.shared.model.Country;

@Component
public class CountryMapper {


    public CountryResponseDto toCountryDto(Country country) {
        return new CountryResponseDto(
                country.getId(),
                country.getName(),
                country.getMobileCode()
        );
    }
}
