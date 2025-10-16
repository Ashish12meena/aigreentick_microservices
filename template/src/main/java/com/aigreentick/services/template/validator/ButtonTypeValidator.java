package com.aigreentick.services.template.validator;

import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.MarketingButtonDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ButtonTypeValidator implements ConstraintValidator<ValidButtonType, MarketingButtonDto> {

    @Override
    public boolean isValid(MarketingButtonDto dto, ConstraintValidatorContext context) {
        if (dto.getType() == null)
            return true; // @NotNull handles null

        boolean valid = true;
        context.disableDefaultConstraintViolation(); // disable generic message

        switch (dto.getType()) {
            case "URL":
                if (dto.getUrl() == null) {
                    context.buildConstraintViolationWithTemplate("URL is required for type URL")
                            .addPropertyNode("url").addConstraintViolation();
                    valid = false;
                }
                if (dto.getPhoneNumber() != null) {
                    context.buildConstraintViolationWithTemplate("Phone number must be null for URL type")
                            .addPropertyNode("phoneNumber").addConstraintViolation();
                    valid = false;
                }
                if (dto.getFlowId() != null) {
                    context.buildConstraintViolationWithTemplate("Flow ID must be null for URL type")
                            .addPropertyNode("flowId").addConstraintViolation();
                    valid = false;
                }
                break;
            case "PHONE_NUMBER":
                if (dto.getPhoneNumber() == null) {
                    context.buildConstraintViolationWithTemplate("Phone number is required for PHONE_NUMBER type")
                            .addPropertyNode("phoneNumber").addConstraintViolation();
                    valid = false;
                }
                if (dto.getUrl() != null) {
                    context.buildConstraintViolationWithTemplate("URL must be null for PHONE_NUMBER type")
                            .addPropertyNode("url").addConstraintViolation();
                    valid = false;
                }
                if (dto.getFlowId() != null) {
                    context.buildConstraintViolationWithTemplate("Flow ID must be null for PHONE_NUMBER type")
                            .addPropertyNode("flowId").addConstraintViolation();
                    valid = false;
                }
                break;
            case "FLOW":
                if (dto.getFlowId() == null) {
                    context.buildConstraintViolationWithTemplate("Flow ID is required for FLOW type")
                            .addPropertyNode("flowId").addConstraintViolation();
                    valid = false;
                }
                if (dto.getUrl() != null) {
                    context.buildConstraintViolationWithTemplate("URL must be null for FLOW type")
                            .addPropertyNode("url").addConstraintViolation();
                    valid = false;
                }
                if (dto.getPhoneNumber() != null) {
                    context.buildConstraintViolationWithTemplate("Phone number must be null for FLOW type")
                            .addPropertyNode("phoneNumber").addConstraintViolation();
                    valid = false;
                }
                break;
            default:
                // QUICK_REPLY, COPY_CODE, CATALOG
                if (dto.getText() == null || dto.getText().isBlank()) {
                    context.buildConstraintViolationWithTemplate("Text is required for " + dto.getType())
                            .addPropertyNode("text").addConstraintViolation();
                    valid = false;
                }
        }

        return valid;
    }
}
