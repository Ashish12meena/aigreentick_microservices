package com.aigreentick.services.common.exceptions.infrastructure;

import com.aigreentick.services.common.enums.ErrorCode;
import com.aigreentick.services.common.exceptions.base.InfrastructureException;

public class ExternalServiceException extends InfrastructureException {
    public ExternalServiceException(String message) {
        super(ErrorCode.EXTERNAL_SERVICE_ERROR.getCode(),ErrorCode.EXTERNAL_SERVICE_ERROR.getNumericCode(), message);
    }
}
