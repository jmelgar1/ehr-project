package com.ehr.auth.controller;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ehr.auth.dto.AuthResponse;
import com.ehr.auth.dto.AuthTokenPair;
import com.ehr.auth.dto.LoginRequest;
import com.ehr.auth.dto.RefreshResponse;
import com.ehr.auth.dto.RegisterRequest;
import com.ehr.auth.service.AuthService;
import com.ehr.auth.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    private static final String JWT_REFRESH_EXPIRATION_PROPERTY = "${jwt.refreshExpiration}";
    private final long refreshExpiration;

    public AuthController(AuthService authService, UserService userService, 
                          @Value(JWT_REFRESH_EXPIRATION_PROPERTY) long refreshExpiration) {
        this.authService = authService;
        this.userService = userService;
        this.refreshExpiration = refreshExpiration;
    }

    // TODO: Add rate limiting to prevent registration abuse
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    // TODO: Add rate limiting to prevent brute force and credential stuffing attacks
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, 
                                              HttpServletResponse response) {
        AuthTokenPair tokenPair = authService.login(request);

        //add refresh token in cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenPair.refreshToken())
        .httpOnly(true)
        .secure(false)
        .sameSite("Strict")
        .path("/api/auth/refresh")
        .maxAge(Duration.ofMillis(refreshExpiration))
        .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(tokenPair.authResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@CookieValue(value = "refreshToken") String refreshToken) {
        String accessToken = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(new RefreshResponse(accessToken));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, 
                                           Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        userService.deleteUser(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
