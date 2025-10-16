package com.aigreentick.services.common.exceptions.infrastructure;

import com.aigreentick.services.common.enums.ErrorCode;
import com.aigreentick.services.common.exceptions.base.InfrastructureException;

public class DatabaseException extends InfrastructureException {

    public DatabaseException(String message) {
        super(ErrorCode.DATABASE_ERROR.getCode(), ErrorCode.DATABASE_ERROR.getNumericCode(), message);
    }

}
