package com.example.aigreentick.services.messaging.flow.service.impl;

import java.util.Collections;
import java.util.Set;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.example.aigreentick.services.messaging.flow.dto.request.FlowRequestDto;
import com.example.aigreentick.services.messaging.flow.dto.request.FlowResponseDto;
import com.example.aigreentick.services.messaging.flow.model.FlowSubmission;
import com.example.aigreentick.services.messaging.flow.service.interfaces.FlowHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Primary
public class DefaultFlowHandler implements FlowHandler {
    private final ObjectMapper objectMapper;

    
    @Override
    public FlowResponseDto handle(FlowRequestDto request, FlowSubmission submission) {
        log.info("Default handler invoked for submissionId={}", submission.getId());
        FlowResponseDto resp = new FlowResponseDto();
        resp.setVersion(request.getVersion());
        resp.setType("noop");
        ObjectNode data = objectMapper.createObjectNode();
        data.put("message", "handled by default handler");
        resp.setData(data);
        return resp;
    }

    @Override
    public Set<String> getSupportedFlowIds() {
        return Collections.emptySet();
    }
}
