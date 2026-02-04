package com.ehr.auth.utils;

import com.ehr.auth.dto.AuthResponse;
import com.ehr.auth.dto.AuthTokenPair;
import com.ehr.auth.dto.LoginRequest;
import com.ehr.auth.dto.RegisterRequest;
import com.ehr.auth.model.enums.UserRole;

public final class AuthControllerTestUtils {

    private AuthControllerTestUtils() {}

    public static AuthResponse authResponse() {
        return new AuthResponse(
            "test-jwt-token", 
            "testuser", 
            UserRole.NURSE
        );
    }

    public static AuthResponse authResponse(String accessToken, String username, UserRole role) {
        return new AuthResponse(
            accessToken,
            username, 
            role
        );
    }

    public static AuthTokenPair authTokenPair() {
        return new AuthTokenPair(
            authResponse(),
            "refreshToken"
        );
    }

    public static AuthTokenPair authTokenPair(AuthResponse authResponse, String refreshToken) {
        return new AuthTokenPair(
            authResponse,
            refreshToken
        );
    }

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
            "correctPassword123"
        );
    }

    public static LoginRequest login(String username, String password) {
        return new LoginRequest(
            username, 
            password
        );
    }
}
