package com.aigreentick.services.shared.mapper;


import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.aigreentick.services.shared.dto.request.blacklist.BlacklistRequestDto;
import com.aigreentick.services.shared.dto.response.blacklist.BlacklistResponseDto;
import com.aigreentick.services.shared.model.Blacklist;




@Component
public class BlacklistMapper {
    public BlacklistResponseDto toBlacklistResponseDto(Blacklist blacklist) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new BlacklistResponseDto(
                blacklist.getMobile(),
                blacklist.getCountryId(),
                blacklist.getReason(),
                blacklist.isBlocked(),
                blacklist.getExpiresAt() != null ? blacklist.getExpiresAt().format(formatter) : null);
    }

    public List<BlacklistResponseDto> toBlacklistDtoList(List<Blacklist> blacklists) {
        return blacklists.stream()
                .map(this::toBlacklistResponseDto)
                .collect(Collectors.toList());
    }

    public Blacklist toBlacklistEntity(BlacklistRequestDto dto) {
        Blacklist blacklist = new Blacklist();
        blacklist.setMobile(dto.getMobile());
        blacklist.setCountryId(dto.getCountryId());
        blacklist.setReason(dto.getReason());
        blacklist.setBlocked(true);
        blacklist.setCreatedAt(Instant.now());
        blacklist.setUpdatedAt(Instant.now());
        return blacklist;
    }

}
