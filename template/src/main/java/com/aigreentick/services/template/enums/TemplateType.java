package com.aigreentick.services.template.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TemplateType {
    COUPON("COUPON"),
    LIMITED_TIME_OFFER("LIMITED_TIME_OFFER");

    private final String value;

    TemplateType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

     @JsonCreator
    public static TemplateType fromValue(String input) {
        if (input == null) {
            return null;
        }
        for (TemplateType category : TemplateType.values()) {
            if (category.value.equalsIgnoreCase(input)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + input);
    }

    public static class constants {
        public static final String COUPON = "COUPON";
        public static final String LIMITED_TIME_OFFER = "LIMITED_TIME_OFFER";
    }
}
