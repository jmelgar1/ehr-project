package com.ehr.auth.utils;

import com.ehr.auth.dto.LoginRequest;
import com.ehr.auth.dto.RegisterRequest;
import com.ehr.auth.model.enums.UserRole;

public final class AuthServiceTestUtils {

    private AuthServiceTestUtils() {}

    public static RegisterRequest register(String username, UserRole role) {
        return new RegisterRequest(
                username,
                username + "@example.com",
                "password12345",
                "Test",
                "User",
                role
        );
    }

    public static LoginRequest login(String username) {
        return new LoginRequest(
            username,
            "password12345"
        );
    }

    public static LoginRequest login(String username, String password) {
        return new LoginRequest(
            username,
            password
        );
    }
}
