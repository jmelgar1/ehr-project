package com.ehr.auth.utils;

import com.ehr.auth.dto.LoginRequest;
import com.ehr.auth.dto.RegisterRequest;
import com.ehr.auth.model.User;
import com.ehr.auth.model.enums.UserRole;

public final class AuthServiceTestUtils {

    private AuthServiceTestUtils() {}

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

    public static User user(String username, UserRole role) {
        return User.builder()
                .username(username)
                .email(username + "@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(role)
                .enabled(true)
                .build();
    }

    public static User user(String username, String password, UserRole role) {
        return User.builder()
                .username(username)
                .email(username + "@example.com")
                .password(password)
                .firstName("Test")
                .lastName("User")
                .role(role)
                .enabled(true)
                .build();
    }
}
