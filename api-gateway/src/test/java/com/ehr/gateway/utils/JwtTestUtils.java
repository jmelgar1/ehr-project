package com.ehr.gateway.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

public final class JwtTestUtils {

    public static final String TEST_SECRET = "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-algorithm";
    public static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private static final SecretKey KEY = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());

    private JwtTestUtils() {
    }

    public static String validToken() {
        return validTokenForUser(TEST_USER_ID);
    }

    public static String validTokenForUser(UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 3600000);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(KEY)
                .compact();
    }

    public static String expiredToken() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() - 1000);

        return Jwts.builder()
                .subject(TEST_USER_ID.toString())
                .issuedAt(new Date(now.getTime() - 3600000))
                .expiration(expiry)
                .signWith(KEY)
                .compact();
    }

    public static String tokenWithWrongSecret() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong-secret-key-that-is-also-at-least-256-bits-long-for-hmac-sha".getBytes());
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 3600000);

        return Jwts.builder()
                .subject(TEST_USER_ID.toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(wrongKey)
                .compact();
    }
}
