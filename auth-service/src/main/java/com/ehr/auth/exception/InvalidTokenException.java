package com.ehr.auth.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Invalid or expired refresh token");
    }
}
