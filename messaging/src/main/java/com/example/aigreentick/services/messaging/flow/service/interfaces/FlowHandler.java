package com.example.aigreentick.services.messaging.flow.service.interfaces;


import java.util.Set;

import com.example.aigreentick.services.messaging.flow.dto.request.FlowRequestDto;
import com.example.aigreentick.services.messaging.flow.dto.request.FlowResponseDto;
import com.example.aigreentick.services.messaging.flow.model.FlowSubmission;


public interface FlowHandler {
    FlowResponseDto handle(FlowRequestDto request, FlowSubmission submission);
    Set<String> getSupportedFlowIds();
}
