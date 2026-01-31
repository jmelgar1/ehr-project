package com.ehr.auth.security;

import com.ehr.auth.model.User;
import com.ehr.auth.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ehr.auth.utils.JwtTokenProviderTestUtils.TEST_USER_ID;
import static com.ehr.auth.utils.JwtTokenProviderTestUtils.user;
import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String TEST_SECRET = "this-is-a-test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha";
    private static final long TEST_ACCESS_EXPIRATION = 3600000L; // 1 hour
    private static final long TEST_REFRESH_EXPIRATION = 2628000000L; // 1 month

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_ACCESS_EXPIRATION, TEST_REFRESH_EXPIRATION);
        testUser = user("testuser", UserRole.THERAPIST);
    }

    @Test
    void givenValidUser_whenGenerateToken_thenReturnsValidJwt() {
        String token = jwtTokenProvider.generateAccessToken(testUser);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void givenValidToken_whenValidateToken_thenReturnsTrue() {
        String token = jwtTokenProvider.generateAccessToken(testUser);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void givenMalformedToken_whenValidateToken_thenReturnsFalse() {
        assertThat(jwtTokenProvider.validateToken("not.a.valid-token")).isFalse();
    }

    @Test
    void givenExpiredToken_whenValidateToken_thenReturnsFalse() {
        JwtTokenProvider shortLivedJwtTokenProvider = new JwtTokenProvider(TEST_SECRET, -1000, TEST_REFRESH_EXPIRATION); // already expired
        String token = shortLivedJwtTokenProvider.generateAccessToken(testUser);

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void givenTokenSignedWithWrongKey_whenValidateToken_thenReturnsFalse() {
        JwtTokenProvider otherJwtTokenProvider = new JwtTokenProvider("another-secret-key-that-is-also-at-least-256-bits-long-for-hmac-sha", TEST_ACCESS_EXPIRATION, TEST_REFRESH_EXPIRATION);
        String token = otherJwtTokenProvider.generateAccessToken(testUser);

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void givenValidToken_whenGetUserIdFromToken_thenReturnsCorrectId() {
        String token = jwtTokenProvider.generateAccessToken(testUser);

        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(TEST_USER_ID);
    }
}
