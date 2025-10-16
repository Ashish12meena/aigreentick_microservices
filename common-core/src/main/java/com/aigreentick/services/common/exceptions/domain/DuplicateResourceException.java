package com.aigreentick.services.common.exceptions.domain;

import com.aigreentick.services.common.enums.ErrorCode;
import com.aigreentick.services.common.exceptions.base.DomainException;

public class DuplicateResourceException extends DomainException{
     public DuplicateResourceException(String message) {
        super(ErrorCode.RESOURCE_CONFLICT.getCode(), ErrorCode.RESOURCE_CONFLICT.getNumericCode(), message);
    }
}
