package com.ehr.auth.exception;

import com.ehr.auth.constant.ExceptionMessages;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super(ExceptionMessages.INVALID_TOKEN);
    }
}
