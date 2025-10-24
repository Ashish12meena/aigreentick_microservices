package com.example.aigreentick.services.messaging.broadcast.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aigreentick.services.messaging.broadcast.client.dto.CountryDto;
import com.example.aigreentick.services.messaging.broadcast.client.service.impl.BlacklistClientImpl;
import com.example.aigreentick.services.messaging.broadcast.dto.request.BroadcastRequestDTO;
import com.example.aigreentick.services.messaging.broadcast.enums.BroadcastStatus;
import com.example.aigreentick.services.messaging.broadcast.model.Broadcast;
import com.example.aigreentick.services.messaging.broadcast.repository.BroadCastRepository;
import com.example.aigreentick.services.messaging.broadcast.util.PhoneNumberUtils;
import com.example.aigreentick.services.messaging.message.dto.build.template.Template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastServiceImpl {
    private final BroadCastRepository broadCastRepository;
    private final BlacklistClientImpl blacklistService;

    public List<String> filterNumberForTemplateMessage(CountryDto country, List<String> mobileNumbers, Long userId) {
        if (country == null || mobileNumbers == null || mobileNumbers.isEmpty() || userId == null) {
            return List.of(); // return empty list if input is invalid
        }

        // Step 1: Normalize numbers to WhatsApp-compatible format
        List<String> formattedMobileNumbers = PhoneNumberUtils.buildNumbersForWhatsappTemplateMessage(
                country.getMobileCode(), mobileNumbers);

        if (formattedMobileNumbers.isEmpty()) {
            return List.of(); // no valid numbers to process
        }

        // Step 2: Filter out blacklisted numbers for this user and country
        List<String> filteredNumbers = blacklistService.filterNonBlacklistedNumbers(
                formattedMobileNumbers, country.getId(), userId);

        return filteredNumbers;
    }

    /**
     * Creates a broadcast and saves it to DB. Throws if schedule date format is
     * wrong.
     */
    @Transactional
    public Broadcast createAndSaveBroadcast(BroadcastRequestDTO dto, List<String> filteredMobileNumbers, Long userId,
            CountryDto country, Template template, Long oraganisationId) {
        Broadcast broadcast = new Broadcast();
        broadcast.setUserId(userId);
        broadcast.setCountryId(country.getId());
        broadcast.setOrganizationId(oraganisationId);
        broadcast.setMedia(dto.isMedia());
        broadcast.setTotalNumbers(dto.getMobileNumbers().size());
        broadcast.setRecipients(String.join(",", filteredMobileNumbers));
        broadcast.setStatus(BroadcastStatus.SENDING);
        if (dto.getScheduledAt() != null) {
            if (dto.getScheduledAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Schedule date must be in the future.");
            }
            broadcast.setScheduleAt(dto.getScheduledAt());
            if (dto.isMedia()) {
                broadcast.setMedia(true);
                broadcast.setMediaId(dto.getMediaId());
                broadcast.setMediaUrl(dto.getMediaUrl());
            }
            broadcast.setStatus(BroadcastStatus.SCHEDULED);
        }
        broadCastRepository.save(broadcast);
        return broadcast;
    }

    public void updateStatusById(BroadcastStatus completed, Long broadcastId) {
        broadCastRepository.updateStatusById(completed, broadcastId);
    }

}
