package com.aigreentick.services.common.exceptions.base;

public abstract class InfrastructureException extends BaseException {
    protected InfrastructureException(String code,int numericCode, String message) {
        super(code, numericCode,message);
    }
}
