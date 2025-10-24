package com.aigreentick.services.shared.controller.country;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aigreentick.services.common.dto.response.ResponseMessage;
import com.aigreentick.services.shared.dto.response.country.CountryResponseDto;
import com.aigreentick.services.shared.service.impl.country.CountryServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/countries")
public class CountryController {
    private final CountryServiceImpl countryService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllCountries() {
        List<CountryResponseDto> countryResponseDto = countryService.getAllCountries();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessage<>("Success", "List of Countries", countryResponseDto));
    }
}
