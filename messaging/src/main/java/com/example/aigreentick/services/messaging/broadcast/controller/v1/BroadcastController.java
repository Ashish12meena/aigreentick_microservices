package com.example.aigreentick.services.messaging.broadcast.controller.v1;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aigreentick.services.common.dto.response.ResponseMessage;
import com.example.aigreentick.services.messaging.broadcast.dto.request.BroadcastRequestDTO;
import com.example.aigreentick.services.messaging.broadcast.service.impl.BroadcastOrchestratorServiceImpl;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/broadcast")
@RequiredArgsConstructor
public class BroadcastController {

    private final BroadcastOrchestratorServiceImpl broadcastOrchestratorService;

    @PostMapping("/dispatch")
    public ResponseMessage<String> broadcastMessages(@RequestBody BroadcastRequestDTO dto,
            Long userId,Long organisationId, Long campaignId) {
        return broadcastOrchestratorService.dispatch(dto, userId,organisationId,campaignId);
    }
    
}