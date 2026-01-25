package com.ehr.auth.utils;

import com.ehr.auth.model.User;
import com.ehr.auth.model.enums.UserRole;

import java.util.UUID;

public final class JwtTokenProviderTestUtils {

    public static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private JwtTokenProviderTestUtils() {}

    public static User user(String username, UserRole role) {
        return User.builder()
                .id(TEST_USER_ID)
                .username(username)
                .email(username + "@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(role)
                .enabled(true)
                .build();
    }

    public static User userWithId(UUID id, String username, UserRole role) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(role)
                .enabled(true)
                .build();
    }
}
