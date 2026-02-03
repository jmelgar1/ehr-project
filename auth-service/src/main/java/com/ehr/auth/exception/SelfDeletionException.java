package com.ehr.auth.exception;

import com.ehr.auth.constant.ExceptionMessages;

public class SelfDeletionException extends RuntimeException {
    public SelfDeletionException() {
        super(ExceptionMessages.SELF_DELETION);
    }
}
