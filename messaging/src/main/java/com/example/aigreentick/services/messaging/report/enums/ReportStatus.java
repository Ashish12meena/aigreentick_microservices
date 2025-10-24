package com.example.aigreentick.services.messaging.report.enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReportStatus {
    ACCEPTED("ACCEPTED"),
    PENDING("PENDING"),
    SUCCESS("PENDING"),
    FAILED("FAILED"),
    BLOCKED("BLOCKED"),
    INVALID_NUMBER("INVALID_NUMBER"),
    DELETED("DELETED");

    private final String value;

    ReportStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value; // Always return lowercase when serializing or printing
    }

    // Flexible deserialization: case-insensitive mapping
    @JsonCreator
    public static ReportStatus fromValue(String input) {
        if (input == null) {
            return null;
        }
        for (ReportStatus category : ReportStatus.values()) {
            if (category.value.equalsIgnoreCase(input)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + input);
    }

    // static constants for reuse
    public static class constants {
        public static final String PENDING = "PENDING";
        public static final String APPROVED = "APPROVED";
        public static final String REJECTED = "REJECTED";
        public static final String FAILED = "FAILED";
        public static final String ACCEPTED = "ACCEPTED";
    }
}
