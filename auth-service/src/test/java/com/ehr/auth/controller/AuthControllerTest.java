package com.ehr.auth.controller;

import com.ehr.auth.config.SecurityConfig;
import com.ehr.auth.exception.DuplicateResourceException;
import com.ehr.auth.exception.GlobalExceptionHandler;
import com.ehr.auth.exception.InvalidCredentialsException;
import com.ehr.auth.exception.ResourceNotFoundException;
import com.ehr.auth.model.enums.UserRole;
import com.ehr.auth.repository.UserRepository;
import com.ehr.auth.security.JwtTokenProvider;
import com.ehr.auth.service.AuthService;
import com.ehr.auth.service.UserService;

import static com.ehr.auth.utils.AuthControllerTestUtils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void givenValidRegistrationData_whenRegister_thenReturns200AndAuthResponse() throws Exception {
        var response = authResponse("jwt-token", "newuser", UserRole.NURSE);
        when(authService.register(any())).thenReturn(response);

        var request = register("newuser", UserRole.NURSE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("NURSE"));
    }

    @Test
    void givenDuplicateUsername_whenRegister_thenReturns409() throws Exception {
        when(authService.register(any())).thenThrow(new DuplicateResourceException("Username already exists"));

        var request = register("existinguser", UserRole.THERAPIST);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturns200AndAuthResponse() throws Exception {
        var response = authResponse("login-token", "testuser", UserRole.ADMIN);
        when(authService.login(any())).thenReturn(response);

        var request = login("testuser");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void givenBadCredentials_whenLogin_thenReturns401() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidCredentialsException());

        var request = login("testuser", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void givenNoAuthentication_whenAccessAuthEndpoints_thenReturnsOk() throws Exception {
        var response = authResponse("token", "user", UserRole.NURSE);
        when(authService.login(any())).thenReturn(response);

        var request = login("user", "pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void givenNoAuthentication_whenAccessProtectedEndpoints_thenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/some-protected-resource"))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenAdminUser_whenDeleteUser_thenReturns204() throws Exception {
        var userId = UUID.randomUUID();
        var adminId = UUID.randomUUID();
        var adminAuth = new UsernamePasswordAuthenticationToken(
                adminId, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        doNothing().when(userService).deleteUser(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/auth/users/{id}", userId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenNonAdminUser_whenDeleteUser_thenReturns403() throws Exception {
        var userId = UUID.randomUUID();
        var nurseId = UUID.randomUUID();
        var nurseAuth = new UsernamePasswordAuthenticationToken(
                nurseId, null, List.of(new SimpleGrantedAuthority("ROLE_NURSE")));

        mockMvc.perform(delete("/api/auth/users/{id}", userId)
                        .with(authentication(nurseAuth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenNonExistentUser_whenDeleteUser_thenReturns404() throws Exception {
        var userId = UUID.randomUUID();
        var adminId = UUID.randomUUID();
        var adminAuth = new UsernamePasswordAuthenticationToken(
                adminId, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/auth/users/{id}", userId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void givenNoAuthentication_whenDeleteUser_thenReturns403() throws Exception {
        var userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/auth/users/{id}", userId))
                .andExpect(status().isForbidden());
    }
}
