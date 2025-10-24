package com.aigreentick.services.common.exceptions.infrastructure;

import com.aigreentick.services.common.enums.ErrorCode;
import com.aigreentick.services.common.exceptions.base.InfrastructureException;

public class FileProcessingException extends InfrastructureException {
    public FileProcessingException(String message) {
        super(ErrorCode.FILE_PROCESSING_ERROR.getCode(),ErrorCode.FILE_PROCESSING_ERROR.getNumericCode(), message);
    }
}
