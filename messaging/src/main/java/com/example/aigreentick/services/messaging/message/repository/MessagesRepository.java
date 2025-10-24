package com.example.aigreentick.services.messaging.message.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.aigreentick.services.messaging.message.model.Messages;

@Repository
public interface MessagesRepository extends MongoRepository<Messages,String>{



    Page<Messages> findByUserIdAndBroadcastIdNotNull(Long userId, Pageable pageable);

    Page<Messages> findByBroadcastId(Long broadcastId, Pageable pageable);
    
}
