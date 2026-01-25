package com.ehr.auth.security;

import com.ehr.auth.model.User;
import com.ehr.auth.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ehr.auth.utils.JwtUtilTestUtils.user;
import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String TEST_SECRET = "this-is-a-test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha";
    private static final long TEST_EXPIRATION = 3600000; // 1 hour

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, TEST_EXPIRATION);
        testUser = user("testuser", UserRole.THERAPIST);
    }

    @Test
    void generateToken_returnsValidJwt() {
        String token = jwtUtil.generateToken(testUser);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        String token = jwtUtil.generateToken(testUser);

        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_returnsFalseForMalformedToken() {
        assertThat(jwtUtil.validateToken("not.a.valid-token")).isFalse();
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        JwtUtil shortLivedJwtUtil = new JwtUtil(TEST_SECRET, -1000); // already expired
        String token = shortLivedJwtUtil.generateToken(testUser);

        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_returnsFalseForTokenWithWrongKey() {
        JwtUtil otherJwtUtil = new JwtUtil("another-secret-key-that-is-also-at-least-256-bits-long-for-hmac-sha", TEST_EXPIRATION);
        String token = otherJwtUtil.generateToken(testUser);

        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    void getUsernameFromToken_extractsCorrectUsername() {
        String token = jwtUtil.generateToken(testUser);

        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo("testuser");
    }

    @Test
    void getRoleFromToken_extractsCorrectRole() {
        String token = jwtUtil.generateToken(testUser);

        assertThat(jwtUtil.getRoleFromToken(token)).isEqualTo(UserRole.THERAPIST);
    }

    @Test
    void getRoleFromToken_worksForAllRoles() {
        for (UserRole role : UserRole.values()) {
            var roleUser = user("user_" + role.name(), role);
            String token = jwtUtil.generateToken(roleUser);

            assertThat(jwtUtil.getRoleFromToken(token)).isEqualTo(role);
        }
    }
}
