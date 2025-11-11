package com.aigreentick.services.shared.service.impl.country;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aigreentick.services.shared.model.Country;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryOrcdhestratorServiceImpl {
    private final CountryServiceImpl countryService;

    // ===== CREATE =====
    @Transactional
    public Country createCountry(Country country) {
        countryService.validateUnique(country);
        return countryService.save(country);
    }

    // ===== UPDATE =====
    @Transactional
    public Country updateCountry(Long id, Country update) {
        Country existing = countryService.findById(id);
        updateFields(existing, update);
        countryService.validateUnique(existing);
        return countryService.save(existing);
    }

    private void updateFields(Country existing, Country update) {
        existing.setName(update.getName());
        existing.setMobileCode(update.getMobileCode());
        existing.setIsoCode(update.getIsoCode());
        existing.setCurrencyCode(update.getCurrencyCode());
        existing.setCurrencySymbol(update.getCurrencySymbol());
        existing.setTimeZone(update.getTimeZone());
        existing.setFlagUrl(update.getFlagUrl());
        existing.setIsActive(update.getIsActive());
        existing.setContinent(update.getContinent());
    }

    // ===== UPDATE =====
    @Transactional
    public void softDeleteCountry(Long id) {
        Country country = countryService.findById(id);
        country.setDeleted(true);
        country.setDeletedAt(Instant.now());
        countryService.save(country);
    }
}
