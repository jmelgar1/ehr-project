package com.ehr.auth.controller;

import static com.ehr.auth.constants.ApiPaths.AUTH_LOGIN_API_PATH;
import static com.ehr.auth.constants.ApiPaths.AUTH_REGISTER_API_PATH;

import com.ehr.auth.config.SecurityConfig;
import com.ehr.auth.exception.DuplicateResourceException;
import com.ehr.auth.exception.GlobalExceptionHandler;
import com.ehr.auth.exception.InvalidCredentialsException;
import com.ehr.auth.model.enums.UserRole;
import com.ehr.auth.repository.UserRepository;
import com.ehr.auth.security.JwtTokenProvider;
import com.ehr.auth.service.AuthService;
import com.ehr.auth.service.PermissionResolverService;

import static com.ehr.auth.utils.AuthControllerTestUtils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PermissionResolverService permissionResolverService;

    @Test
    void givenValidRegistrationData_whenRegister_thenReturns200AndAuthResponse() throws Exception {
        var response = authResponse("jwt-token", "newuser", UserRole.NURSE);
        when(authService.register(any())).thenReturn(response);

        var request = register("newuser", UserRole.NURSE);

        mockMvc.perform(post(AUTH_REGISTER_API_PATH)
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

        mockMvc.perform(post(AUTH_REGISTER_API_PATH)
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

        mockMvc.perform(post(AUTH_LOGIN_API_PATH)
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

        mockMvc.perform(post(AUTH_LOGIN_API_PATH)
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

        mockMvc.perform(post(AUTH_LOGIN_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
