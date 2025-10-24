package com.aigreentick.services.common.exceptions.base;

public class ClientSideException extends BaseException {

    public ClientSideException(String code, int numericCode, String message) {
        super(code, numericCode, message);
    }
    
}
