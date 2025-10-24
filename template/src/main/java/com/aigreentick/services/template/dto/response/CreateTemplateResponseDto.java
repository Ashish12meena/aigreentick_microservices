package com.aigreentick.services.template.dto.response;

import java.util.List;

import com.aigreentick.services.template.dto.buildTemplateForRequest.BaseTemplateRequestDto;
import com.aigreentick.services.template.dto.request.TemplateTextDto;

import lombok.Data;

@Data
public class CreateTemplateResponseDto {
    private BaseTemplateRequestDto template;
    private List<TemplateTextDto> variables;
}
