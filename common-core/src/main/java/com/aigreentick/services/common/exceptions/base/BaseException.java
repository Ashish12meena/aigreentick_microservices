package com.aigreentick.services.common.exceptions.base;


public abstract class BaseException extends RuntimeException {

    private final String code;       // String code
    private final int numericCode;   // Numeric code

    public BaseException(String code, int numericCode, String message) {
        super(message);
        this.code = code;
        this.numericCode = numericCode;
    }

    public String getCode() {
        return code;
    }

    public int getNumericCode() {
        return numericCode; 
    }
}

