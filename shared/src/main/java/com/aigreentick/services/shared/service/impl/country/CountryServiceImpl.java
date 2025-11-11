package com.aigreentick.services.shared.service.impl.country;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aigreentick.services.common.exceptions.domain.DuplicateResourceException;
import com.aigreentick.services.shared.dto.response.country.CountryResponseDto;
import com.aigreentick.services.shared.exception.CountryNotFoundException;
import com.aigreentick.services.shared.mapper.CountryMapper;
import com.aigreentick.services.shared.model.Country;
import com.aigreentick.services.shared.repository.CountryRepository;
import com.aigreentick.services.common.service.base.jpa.JpaBaseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryServiceImpl extends JpaBaseService<Country, Long> {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    // ===== GET ALL COUNTRIES =====
    @Transactional(readOnly = true)
    public List<CountryResponseDto> getAllCountries() {
        return countryRepository.findAll().stream()
                .filter(c -> !Boolean.TRUE.equals(c.isDeleted()))
                .map(countryMapper::toCountryDto)
                .collect(Collectors.toList());
    }

    // ===== GET BY ID =====
    @Transactional(readOnly = true)
    public CountryResponseDto getCountryById(Long id) {
        Country country = findById(id);
        return countryMapper.toCountryDto(country);
    }

    @Transactional(readOnly = true)
    public Country findById(Long id) {
        return countryRepository.findById(id)
                .filter(c -> !Boolean.TRUE.equals(c.isDeleted()))
                .orElseThrow(() -> new CountryNotFoundException("Country not found with id " + id));
    }

    // ===== GET BY NAME =====
    @Transactional(readOnly = true)
    public CountryResponseDto getCountryByName(String name) {
        Country country = countryRepository.findByNameIgnoreCaseAndIsDeletedIsFalse(name)
                .orElseThrow(() -> new CountryNotFoundException("Country not found with name: " + name));
        return countryMapper.toCountryDto(country);
    }

    @Transactional(readOnly = true)
    public Country getByName(String name) {
        return countryRepository.findByNameIgnoreCaseAndIsDeletedIsFalse(name)
                .orElseThrow(() -> new CountryNotFoundException("Country not found with name " + name));
    }

    // ===== GET BY MOBILE CODE =====
    @Transactional(readOnly = true)
    public CountryResponseDto getCountryByCode(String code) {
        Country country = countryRepository.findByMobileCodeAndIsDeletedIsFalse(code)
                .orElseThrow(() -> new CountryNotFoundException("Country not found with code: " + code));
        return countryMapper.toCountryDto(country);
    }

    @Transactional(readOnly = true)
    public Long getCountryIdByMobileCode(String mobileCode) {
        return countryRepository.findIdByMobileCodeAndIsDeletedIsFalse(mobileCode)
                .orElseThrow(() -> new CountryNotFoundException("Country not found by mobile code: " + mobileCode));
    }

   void validateUnique(Country country) {
        if (country.getName() != null && countryRepository.existsByNameIgnoreCaseAndIsDeletedIsFalse(country.getName())) {
            throw new DuplicateResourceException("Country already exists with name: " + country.getName());
        }
        if (country.getIsoCode() != null && countryRepository.existsByIsoCodeAndIsDeletedIsFalse(country.getIsoCode())) {
            throw new DuplicateResourceException("Country already exists with ISO code: " + country.getIsoCode());
        }
    }

    // ===== DELETE =====
    @Transactional
    public void deleteById(Long id) {
        Country country = findById(id);
        countryRepository.delete(country);
    }

    @Transactional
    public void delete(Country country) {
        if (country.getId() == null || !existsById(country.getId())) {
            throw new CountryNotFoundException("Country not found for deletion");
        }
        countryRepository.delete(country);
    }

    

    // ===== EXISTENCE CHECKS =====
    @Transactional(readOnly = true)
    public boolean existsByIsoCode(String isoCode) {
        return countryRepository.existsByIsoCodeAndIsDeletedIsFalse(isoCode);
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return countryRepository.existsByNameIgnoreCaseAndIsDeletedIsFalse(name);
    }

    @Override
    protected JpaRepository<Country, Long> getRepository() {
        return countryRepository;
    }
}
