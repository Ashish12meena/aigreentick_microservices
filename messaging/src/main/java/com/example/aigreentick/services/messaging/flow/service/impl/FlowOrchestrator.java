package com.example.aigreentick.services.messaging.flow.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.aigreentick.services.messaging.flow.dto.request.FlowRequestDto;
import com.example.aigreentick.services.messaging.flow.dto.request.FlowResponseDto;
import com.example.aigreentick.services.messaging.flow.model.FlowSubmission;
import com.example.aigreentick.services.messaging.flow.service.interfaces.FlowHandler;


@Component
public class FlowOrchestrator {
    private final Map<String, FlowHandler> handlers = new HashMap<>();
    private final FlowHandler defaultHandler;

    public FlowOrchestrator(List<FlowHandler> handlerList, FlowHandler defaultHandler) {
        handlerList.forEach(h -> h.getSupportedFlowIds().forEach(id -> handlers.put(id, h)));
        this.defaultHandler = defaultHandler;
    }

    public FlowResponseDto route(FlowRequestDto request, FlowSubmission submission) {
        if (request.getFlowId() != null && handlers.containsKey(request.getFlowId())) {
            return handlers.get(request.getFlowId()).handle(request, submission);
        }
        return defaultHandler.handle(request, submission);
    }
}
