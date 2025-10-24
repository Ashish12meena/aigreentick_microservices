package com.aigreentick.services.common.exceptions.base;

public abstract class DomainException extends BaseException {
    protected DomainException(String code,int numericCode, String message) {
        super(code, numericCode,message);
    }
}
