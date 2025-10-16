package com.example.aigreentick.services.messaging.message.model.content.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitedTimeOffer {
    private Long expirationTimeMs;
}
