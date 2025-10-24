package com.example.aigreentick.services.messaging.flow.service.impl;

import java.util.Set;

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
@Slf4j
@RequiredArgsConstructor
public class DeliveryFlowHandler implements FlowHandler {
    private final ObjectMapper objectMapper;

    @Override
    public FlowResponseDto handle(FlowRequestDto request, FlowSubmission submission) {
        log.info("DeliveryFlowHandler: submissionId={} wa={} screen={} action={}", submission.getId(),
                submission.getWaId(), request.getScreen(), request.getAction());

        // Example business logic: save to orders, notify ops, etc. Implement your own.
        FlowResponseDto resp = new FlowResponseDto();
        resp.setVersion(request.getVersion());
        resp.setType("next_screen");
        ObjectNode data = objectMapper.createObjectNode();
        data.put("confirmation", "Your delivery request has been received. Our agent will contact you soon.");
        resp.setData(data);
        return resp;
    }

    @Override
    public Set<String> getSupportedFlowIds() {
        return Set.of("delivery_flow_v1", "delivery_flow_v2");
    }
}
