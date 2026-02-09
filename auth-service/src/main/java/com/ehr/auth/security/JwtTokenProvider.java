package com.ehr.auth.security;

import com.ehr.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String JWT_SECRET_PROPERTY = "${jwt.secret}";
    private static final String JWT_ACCESS_TOKEN_EXPIRATION = "${jwt.accessExpiration}";
    private static final String JWT_REFRESH_TOKEN_EXPIRATION = "${jwt.refreshExpiration}";

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtTokenProvider(@Value(JWT_SECRET_PROPERTY) String secret, 
                            @Value(JWT_ACCESS_TOKEN_EXPIRATION) long accessExpiration, 
                            @Value(JWT_REFRESH_TOKEN_EXPIRATION) long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(User user) {
        return createToken(user, accessExpiration);
    }

    public String generateRefreshToken(User user) {
        return createToken(user, refreshExpiration);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        return UUID.fromString(claims.getSubject());
    }

    private String createToken(User user, long expiry) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiry))
                .signWith(key)
                .compact();
    }
}
