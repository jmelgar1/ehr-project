package com.ehr.auth.service;

import com.ehr.auth.constant.ExceptionMessages;
import com.ehr.auth.exception.DuplicateResourceException;
import com.ehr.auth.exception.InvalidCredentialsException;
import com.ehr.auth.exception.InvalidTokenException;
import com.ehr.auth.exception.ResourceNotFoundException;
import com.ehr.auth.model.User;
import com.ehr.auth.model.enums.UserRole;
import com.ehr.auth.repository.UserRepository;
import com.ehr.auth.security.JwtTokenProvider;

import io.jsonwebtoken.JwtException;

import static com.ehr.auth.utils.AuthServiceTestUtils.*;
import static com.ehr.auth.utils.UserServiceTestUtils.user;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void givenValidRegistrationRequest_whenRegister_thenSavesUserAndReturnsAuthResponse() {
        var request = register("newuser", UserRole.NURSE);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password12345")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn("test-jwt-token");

        var response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("test-jwt-token");
        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.role()).isEqualTo(UserRole.NURSE);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void givenExistingUsername_whenRegister_thenThrowsDuplicateResourceException() {
        var request = register("existinguser", UserRole.THERAPIST);

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(ExceptionMessages.USERNAME_EXISTS);
    }

    @Test
    void givenExistingEmail_whenRegister_thenThrowsDuplicateResourceException() {
        var request = register("existingEmail", UserRole.ADMIN);

        when(userRepository.existsByUsername("existingEmail")).thenReturn(false);
        when(userRepository.existsByEmail("existingEmail@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(ExceptionMessages.EMAIL_EXISTS);
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnsAuthResponse() {
        var request = login("testuser");
        var testUser = user("testuser", "encodedPassword", UserRole.COORDINATOR);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password12345", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("login-jwt-token");

        var response = authService.login(request);

        assertThat(response.authResponse().accessToken()).isEqualTo("login-jwt-token");
        assertThat(response.authResponse().username()).isEqualTo("testuser");
        assertThat(response.authResponse().role()).isEqualTo(UserRole.COORDINATOR);
    }

    @Test
    void givenUnknownUser_whenLogin_thenThrowsInvalidCredentialsException() {
        var request = login("unknown");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void givenWrongPassword_whenLogin_thenThrowsInvalidCredentialsException() {
        var request = login("testuser", "wrongpassword");
        var testUser = user("testuser", "encodedPassword", UserRole.RESEARCHER);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void givenValidRefreshToken_whenRefreshAccessToken_thenReturnNewAccessToken() {
        var refreshToken = "refreshToken";
        var accessToken = "newAccessToken";
        var userId = UUID.randomUUID();
        var testUser = user("testuser", "encodedPassword", UserRole.COORDINATOR);


        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn(accessToken);

        var response = authService.refreshAccessToken(refreshToken);

        assertThat(response).isEqualTo(accessToken);
    }

    @Test
    void givenNonExistentUser_whenRefreshAccessToken_thenReturnUserNotFound() {
        var refreshToken = "refreshToken";
        var userId = UUID.randomUUID();

        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenThrow(new ResourceNotFoundException(ExceptionMessages.USER_NOT_FOUND));

        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenInvalidToken_whenRefreshAccessToken_thenInvalidTokenException() {
        var refreshToken = "invalidRefreshToken";

        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenThrow(new JwtException("error"));

        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(InvalidTokenException.class);
    }
}
