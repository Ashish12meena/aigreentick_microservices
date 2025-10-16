package com.aigreentick.services.shared.service.impl.blacklist;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.aigreentick.services.common.dto.response.ResponseMessage;
import com.aigreentick.services.shared.constants.CountryConstants;
import com.aigreentick.services.shared.dto.request.blacklist.BlacklistRequestDto;
import com.aigreentick.services.shared.dto.response.blacklist.BlacklistResponseDto;
import com.aigreentick.services.shared.enums.blacklist.BlacklistType;
import com.aigreentick.services.shared.mapper.BlacklistMapper;
import com.aigreentick.services.shared.model.Blacklist;
import com.aigreentick.services.shared.repository.BlacklistRepository;
import com.aigreentick.services.shared.service.impl.country.CountryServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl  {
    private final BlacklistRepository blacklistRepository;
    private final BlacklistMapper blacklistMapper;
    private final CountryServiceImpl countryService;
    
    public List<String> filterNonBlacklistedNumbers(List<String> mobiles, Long countryId, Long senderId) {
        if (mobiles == null || mobiles.isEmpty())
            return Collections.emptyList();

        List<Blacklist> blacklisted = blacklistRepository.findBlacklistedNumbers(mobiles, countryId, senderId);

        Set<String> blacklistedSet = blacklisted.stream()
                .map(Blacklist::getMobile)
                .collect(Collectors.toSet());

        return mobiles.stream()
                .filter(m -> !blacklistedSet.contains(m))
                .collect(Collectors.toList());
    }

    
    public ResponseMessage<String> addToBlacklist(BlacklistRequestDto blacklistRequestDto, Long senderId) {
        if (isAlreadyBlacklistedByUser(blacklistRequestDto.getMobile(), senderId)) {
            return new ResponseMessage<>("failed", "Already blacklisted", null);
        }

        Blacklist blacklist = blacklistMapper.toBlacklistEntity(blacklistRequestDto);
        blacklist.setSenderId(senderId);
        blacklist.setType(BlacklistType.BLOCKED_BY_USER);
        blacklistRepository.save(blacklist);
        return new ResponseMessage<>("success", "User blacklisted sucesfully", null);
    }

    
    public ResponseMessage<String> addToBlacklistByAdmin(BlacklistRequestDto blacklistRequestDto) {
        Long countryId = blacklistRequestDto.getCountryId();

        // Dynamically resolve Global ID if not provided
        if (countryId == null) {
            countryId = countryService.getCountryIdByMobileCode(CountryConstants.GLOBAL_COUNTRY_CODE);
        }
        if (isAlreadyBlacklistedByAdmin(blacklistRequestDto.getMobile(), countryId)) {
            return new ResponseMessage<>("failed", "User blacklisted succesfully by admin", null);
        }
        Blacklist blacklist = blacklistMapper.toBlacklistEntity(blacklistRequestDto);
        blacklist.setType(BlacklistType.BLOCKED_BY_ADMIN);
        blacklistRepository.save(blacklist);
        return new ResponseMessage<>("success", "User blacklisted successfully by admin", null);
    }

    
    public Page<BlacklistResponseDto> getFilteredBlacklistEntries(String mobile, Long countryId, BlacklistType type,
            Boolean isBlocked, Boolean isExpired, Pageable pageable) {

        Page<Blacklist> page = blacklistRepository.findFilteredBlacklistEntries(
                mobile, countryId, type, isBlocked, isExpired, pageable);
        return page.map(blacklistMapper::toBlacklistResponseDto);
    }

    public boolean isAlreadyBlacklistedByAdmin(String mobile, Long countryId) {
        return blacklistRepository.existsByAdmin(mobile, countryId, BlacklistType.BLOCKED_BY_ADMIN);
    }

    public boolean isAlreadyBlacklistedByUser(String mobile, Long senderId) {
         return blacklistRepository.existsByUser(mobile, BlacklistType.BLOCKED_BY_USER, senderId);
    }

}

