package com.ehr.auth.exception;

public class SelfDeletionException extends RuntimeException {
    public SelfDeletionException() {
        super("Cannot delete your own account");
    }
}
