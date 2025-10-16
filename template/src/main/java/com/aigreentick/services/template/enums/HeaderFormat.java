package com.aigreentick.services.template.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HeaderFormat {
    TEXT("TEXT"),
    IMAGE("IMAGE"),
    VIDEO("VIDEO"),
    DOCUMENT("DOCUMENTATION"),
    LOCATION("LOCATION"),
    PRODUCT("PRODUCT");

    public static class constants {
        public static final String TEXT = "TEXT";
        public static final String IMAGE = "IMAGE";
        public static final String VIDEO = "VIDEO";
        public static final String DOCUMENT = "DOCUMENT";
        public static final String LOCATION = "LOCATION";
        public static final String PRODUCT = "PRODUCT";
    }

    private final String value;

    HeaderFormat(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static HeaderFormat fromValue(String value) {
        for (HeaderFormat type : HeaderFormat.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown component type: " + value);
    }
}
