package com.example.aigreentick.services.messaging.broadcast.dto.request;

import java.time.LocalDateTime;
import java.util.List;


import jakarta.validation.constraints.Future;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class BroadcastRequestDTO extends BuildTemplateRequestDto {
    private String temlateId;
    private String campanyName;
    private Long countryId;
    private List<String> mobileNumbers;

    
    @Future(message = "Schedule date must be in the future")
    private LocalDateTime scheduledAt;    
}
    