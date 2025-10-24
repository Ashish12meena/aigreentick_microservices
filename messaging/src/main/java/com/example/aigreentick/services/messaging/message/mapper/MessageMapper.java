package com.example.aigreentick.services.messaging.message.mapper;


import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.example.aigreentick.services.messaging.message.dto.request.MessageRequest;
import com.example.aigreentick.services.messaging.message.dto.response.ChatResponseDto;
import com.example.aigreentick.services.messaging.message.dto.response.SendTemplateMessageResponse;
import com.example.aigreentick.services.messaging.message.model.Messages;
import com.example.aigreentick.services.messaging.message.model.content.Content;
import com.example.aigreentick.services.messaging.message.model.content.Interactive;
import com.example.aigreentick.services.messaging.message.model.content.interactive.InteractiveDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageMapper {
   private final ObjectMapper objectMapper;

   public Messages toEntity() {
      return Messages.builder()
            .build();
   }

   public Messages toEntity(MessageRequest messageRequest,
         SendTemplateMessageResponse response,  
         String payload,
         String phoneNumberId) {
      String responseString = null;
      try {
         responseString = objectMapper.writeValueAsString(response);
      } catch (JsonProcessingException e) {
         e.printStackTrace();
      }
      return Messages.builder()
            .to(messageRequest.getTo())
            .phoneNumberId(phoneNumberId)
            .type(messageRequest.getType())
            .waId(response.getContacts().get(0).getWaId())
            .payload(payload)
            .response(responseString)
            .messageId(response.getMessages().get(0).getId())
            .content(buildContent(messageRequest))
            .createdAt(LocalDateTime.now())
            .build();
   }

   private Content buildContent(MessageRequest messageRequest) {
      Content content = new Content();
      content.setInteractive(buildInteractive(messageRequest.getInteractive()));
      content.setAudio(messageRequest.getAudio());
      content.setContacts(messageRequest.getContacts());
      content.setDocument(messageRequest.getDocument());
      content.setImage(messageRequest.getImage());
      return content;
   }

   private Interactive buildInteractive(InteractiveDto interactiveDto) {
      Interactive interactive = new Interactive();
      interactive.setAction(interactive.getAction());
      interactive.setBody(interactive.getBody());
      interactive.setType(interactive.getType());
      return interactive;
   }

   public ChatResponseDto toChatResponseDto(Messages messages) {
      return ChatResponseDto.builder()
            .id(messages.getId())
            .sendAt(messages.getCreatedAt())
            .build();
   }
}
