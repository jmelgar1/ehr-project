package com.ehr.auth.service;

import com.ehr.auth.constant.ExceptionMessages;
import com.ehr.auth.dto.AuthResponse;
import com.ehr.auth.dto.AuthTokenPair;
import com.ehr.auth.dto.LoginRequest;
import com.ehr.auth.dto.RegisterRequest;
import com.ehr.auth.exception.DuplicateResourceException;
import com.ehr.auth.exception.InvalidCredentialsException;
import com.ehr.auth.exception.InvalidTokenException;
import com.ehr.auth.exception.ResourceNotFoundException;
import com.ehr.auth.model.User;
import com.ehr.auth.repository.UserRepository;
import com.ehr.auth.security.JwtTokenProvider;

import io.jsonwebtoken.JwtException;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException(ExceptionMessages.USERNAME_EXISTS);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(ExceptionMessages.EMAIL_EXISTS);
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .enabled(true)
                .build();

        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        return new AuthResponse(accessToken, user.getUsername(), user.getRole());
    }

    public AuthTokenPair login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        AuthResponse authResponse = new AuthResponse(accessToken, user.getUsername(), user.getRole());
        return new AuthTokenPair(authResponse, refreshToken);
    }

    
    public String refreshAccessToken(String refreshToken) {
        try {
            //we implicitly validate tokens inside getUserIdFromToken
            UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(ExceptionMessages.USER_NOT_FOUND));
            return jwtTokenProvider.generateAccessToken(user);
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }
}
