package com.ehr.auth.dto;

public record AuthTokenPair(
    AuthResponse authResponse,
    String refreshToken) {
}
