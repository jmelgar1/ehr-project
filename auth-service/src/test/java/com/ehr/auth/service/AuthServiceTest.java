package com.ehr.auth.service;

import com.ehr.auth.dto.AuthResponse;
import com.ehr.auth.dto.LoginRequest;
import com.ehr.auth.dto.RegisterRequest;
import com.ehr.auth.exception.DuplicateResourceException;
import com.ehr.auth.exception.InvalidCredentialsException;
import com.ehr.auth.model.User;
import com.ehr.auth.model.enums.UserRole;
import com.ehr.auth.repository.UserRepository;
import com.ehr.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void register_succeeds_savesUserAndReturnsAuthResponse() {
        RegisterRequest request = new RegisterRequest(
                "newuser", "new@example.com", "password123",
                "New", "User", UserRole.NURSE
        );

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("test-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("test-jwt-token");
        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.role()).isEqualTo(UserRole.NURSE);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsDuplicateResourceException_forExistingUsername() {
        RegisterRequest request = new RegisterRequest(
                "existinguser", "new@example.com", "password123",
                "New", "User", UserRole.THERAPIST
        );

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void register_throwsDuplicateResourceException_forExistingEmail() {
        RegisterRequest request = new RegisterRequest(
                "newuser", "existing@example.com", "password123",
                "New", "User", UserRole.ADMIN
        );

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void login_succeeds_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .role(UserRole.COORDINATOR)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("login-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("login-jwt-token");
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.role()).isEqualTo(UserRole.COORDINATOR);
    }

    @Test
    void login_throwsInvalidCredentialsException_forUnknownUser() {
        LoginRequest request = new LoginRequest("unknown", "password123");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throwsInvalidCredentialsException_forWrongPassword() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .role(UserRole.RESEARCHER)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
