package com.aigreentick.services.common.exceptions.client;

import com.aigreentick.services.common.enums.ErrorCode;
import com.aigreentick.services.common.exceptions.base.ClientSideException;

public class ResourceNotFoundException extends ClientSideException{

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ErrorCode.RESOURCE_NOT_FOUND.getNumericCode(), message);
    }
    
}
    