package com.example.aigreentick.services.messaging.message.controller.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aigreentick.services.messaging.message.dto.request.MessageRequest;
import com.example.aigreentick.services.messaging.message.dto.response.ChatResponseDto;
import com.example.aigreentick.services.messaging.message.service.impl.MessagesOrchestratorServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class MessageController {
    private final MessagesOrchestratorServiceImpl chatService;

    @PostMapping("/message")
    public ResponseEntity<?> message(
            @RequestBody MessageRequest messageRequest,
            @RequestBody Long userId) {
        ChatResponseDto response = chatService.sendMessage(messageRequest, userId);
        return ResponseEntity.ok().body(response);
    }
}
