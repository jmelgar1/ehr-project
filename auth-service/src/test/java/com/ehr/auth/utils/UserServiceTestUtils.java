package com.ehr.auth.utils;

import com.ehr.auth.model.User;
import com.ehr.auth.model.enums.UserRole;

public final class UserServiceTestUtils {

    private UserServiceTestUtils() {}

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
