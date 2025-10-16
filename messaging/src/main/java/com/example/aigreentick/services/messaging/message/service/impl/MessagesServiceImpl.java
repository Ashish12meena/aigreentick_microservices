package com.example.aigreentick.services.messaging.message.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.aigreentick.services.common.service.base.mongo.MongoBaseService;
import com.example.aigreentick.services.messaging.message.model.Messages;
import com.example.aigreentick.services.messaging.message.repository.MessagesRepository;;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagesServiceImpl extends MongoBaseService<Messages, String> {
    private final MessagesRepository messagesRepository;

    @Override
    protected MessagesRepository getRepository() {
        return messagesRepository;
    }

    

    public void saveMessagesChunks(List<List<Messages>> chunkedMessages) {
        for (List<Messages> chunk : chunkedMessages) {
            messagesRepository.saveAll(chunk);
            log.info("Saved {} reports in database", chunk.size());
        }
    }

     public Page<Messages> findByUserIdAndBroadcastIdNotNull(Long userId, Pageable pageable) {
        return messagesRepository.findByUserIdAndBroadcastIdNotNull(userId, pageable);
    }

    public Page<Messages> findByBroadcastId(Long broadcastId, Pageable pageable) {
        return messagesRepository.findByBroadcastId(broadcastId, pageable);
    }

}
