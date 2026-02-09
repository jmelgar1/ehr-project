package com.ehr.auth.constant;

public final class ExceptionMessages {

    private ExceptionMessages() {}

    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String INVALID_TOKEN = "Invalid or expired refresh token";
    public static final String SELF_DELETION = "Cannot delete your own account";
    public static final String USERNAME_EXISTS = "Username already exists";
    public static final String EMAIL_EXISTS = "Email already exists";
    public static final String USER_NOT_FOUND = "User not found";
}
