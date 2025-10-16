package com.aigreentick.services.template.dto.buildTemplateForRequest;

import java.util.List;

import com.aigreentick.services.template.dto.buildTemplateForRequest.authentication.AuthTemplateRequestDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.MarketingTemplateRequestDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.utility.UtilityTemplateRequestDto;
import com.aigreentick.services.template.enums.TemplateCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "category", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = AuthTemplateRequestDto.class, name = TemplateCategory.constants.AUTHENTICATION)   ,
    @JsonSubTypes.Type(value = MarketingTemplateRequestDto.class, name = TemplateCategory.constants.MARKETING),
    @JsonSubTypes.Type(value = UtilityTemplateRequestDto.class, name = TemplateCategory.constants.UTILITY)
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseTemplateRequestDto {
    private String name;
    private String language;
    private TemplateCategory category;

    //  Polymorphic method to be overridden
    public abstract List<? extends BaseComponentDto> getComponents();
    private String id;
    private String status;
}
