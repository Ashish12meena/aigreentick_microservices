package com.aigreentick.services.common.enums;

public enum ErrorCode {

    // ================== Infrastructure Errors (5xx) ==================
    DATABASE_ERROR("DATABASE_ERROR", 1001),
    CACHE_ERROR("CACHE_ERROR", 1002),
    FILE_STORAGE_ERROR("FILE_STORAGE_ERROR", 1003),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", 1004),
    FILE_PROCESSING_ERROR("FILE_PROCESSING_ERROR", 1005),

    // ================== Server Errors (5xx) ==================
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", 1500),

    // ================== Client Errors (4xx) ==================
    INPUT_VALIDATION_ERROR("INPUT_VALIDATION_ERROR", 2001),

    // ================== Domain/Business Errors (4xx) ==================
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", 2002),
    RESOURCE_CONFLICT("RESOURCE_CONFLICT", 4001),

    // ================== Security / Auth Errors (4xx) ==================
    UNAUTHORIZED("UNAUTHORIZED", 3001),
    FORBIDDEN("FORBIDDEN", 3002),

    // ================== Resource Errors (4xx) ==================
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", 4040);

    private final String code;
    private final int numericCode;

    ErrorCode(String code, int numericCode) {
        this.code = code;
        this.numericCode = numericCode;
    }

    public String getCode() {
        return code;
    }

    public int getNumericCode() {
        return numericCode;
    }

    public static ErrorCode fromCode(String code) {
        for (ErrorCode e : ErrorCode.values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return INTERNAL_SERVER_ERROR; // fallback
    }

    public static ErrorCode fromNumericCode(int numericCode) {
        for (ErrorCode e : ErrorCode.values()) {
            if (e.numericCode == numericCode) {
                return e;
            }
        }
        return INTERNAL_SERVER_ERROR; // fallback
    }
}
