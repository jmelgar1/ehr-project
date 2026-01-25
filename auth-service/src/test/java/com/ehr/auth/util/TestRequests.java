package com.ehr.auth.util;

import com.ehr.auth.dto.LoginRequest;
import com.ehr.auth.dto.RegisterRequest;
import com.ehr.auth.model.enums.UserRole;

public final class TestRequests {

    private TestRequests() {}

    public static RegisterRequest register() {
        return new RegisterRequest(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                UserRole.NURSE
        );
    }

    public static RegisterRequest register(String username) {
        return new RegisterRequest(
                username,
                username + "@example.com",
                "password123",
                "Test",
                "User",
                UserRole.NURSE
        );
    }

    public static RegisterRequest register(String username, UserRole role) {
        return new RegisterRequest(
                username,
                username + "@example.com",
                "password123",
                "Test",
                "User",
                role
        );
    }

    public static LoginRequest login() {
        return new LoginRequest(
                "testuser",
                "password123"
        );
    }

    public static LoginRequest login(String username) {
        return new LoginRequest(
                username,
                "password123"
        );
    }

    public static LoginRequest login(String username, String password) {
        return new LoginRequest(
                username,
                password
        );
    }
}
