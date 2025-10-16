package com.aigreentick.services.shared.controller.blacklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aigreentick.services.common.dto.response.ResponseMessage;
import com.aigreentick.services.shared.dto.request.blacklist.BlacklistRequestDto;
import com.aigreentick.services.shared.service.impl.blacklist.BlacklistServiceImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/blacklists")
public class BlacklistController {

    @Autowired
    BlacklistServiceImpl blacklistService;

    // // blacklist a user
    @PostMapping("/add")
    public ResponseEntity<ResponseMessage<String>> add(@RequestBody @Valid BlacklistRequestDto request,
            Long senderId) {
        return ResponseEntity.ok(blacklistService.addToBlacklist(request, senderId));
    }
}
