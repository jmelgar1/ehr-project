package com.ehr.auth.exception;

import com.ehr.auth.constant.ExceptionMessages;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super(ExceptionMessages.INVALID_CREDENTIALS);
    }
}
