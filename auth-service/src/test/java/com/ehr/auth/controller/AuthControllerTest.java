package com.ehr.auth.controller;

import static com.ehr.auth.utils.AuthControllerTestUtils.authResponse;
import static com.ehr.auth.utils.AuthControllerTestUtils.authTokenPair;
import static com.ehr.auth.utils.AuthControllerTestUtils.login;
import static com.ehr.auth.utils.AuthControllerTestUtils.register;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ehr.auth.config.SecurityConfig;
import com.ehr.auth.constant.ExceptionMessages;
import com.ehr.auth.exception.DuplicateResourceException;
import com.ehr.auth.exception.GlobalExceptionHandler;
import com.ehr.auth.exception.InvalidCredentialsException;
import com.ehr.auth.exception.ResourceNotFoundException;
import com.ehr.auth.exception.SelfDeletionException;
import com.ehr.auth.model.enums.UserRole;
import com.ehr.auth.repository.UserRepository;
import com.ehr.auth.security.JwtTokenProvider;
import com.ehr.auth.service.AuthService;
import com.ehr.auth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        var response = authResponse("accessToken", "newuser", UserRole.NURSE);
        var request = register("newuser", UserRole.NURSE);

        when(authService.register(eq(request))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void givenDuplicateUsername_whenRegister_thenReturns409() throws Exception {
        var request = register("existinguser", UserRole.THERAPIST);

        when(authService.register(eq(request))).thenThrow(new DuplicateResourceException(ExceptionMessages.USERNAME_EXISTS));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.USERNAME_EXISTS));
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturns200AndAuthResponse() throws Exception {
        var response = authTokenPair(authResponse(), "refreshToken");
        var request = login("testuser");

        when(authService.login(eq(request))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response.authResponse())));
    }

    @Test
    void givenBadCredentials_whenLogin_thenReturns401() throws Exception {
        var request = login("testuser", "wrongpassword");

        when(authService.login(eq(request))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.INVALID_CREDENTIALS));
    }

    @Test
    void givenNoAuthentication_whenAccessAuthEndpoints_thenReturnsOk() throws Exception {
        var response = authTokenPair(authResponse(), "refreshToken");
        var request = login("user", "pass");

        when(authService.login(eq(request))).thenReturn(response);

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
        var adminAuth = new UsernamePasswordAuthenticationToken(adminId, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        doNothing().when(userService).deleteUser(eq(userId), eq(adminId));

        mockMvc.perform(delete("/api/auth/users/{id}", userId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenNonAdminUser_whenDeleteUser_thenReturns403() throws Exception {
        var userId = UUID.randomUUID();
        var nurseId = UUID.randomUUID();
        var nurseAuth = new UsernamePasswordAuthenticationToken(nurseId, null, List.of(new SimpleGrantedAuthority("ROLE_NURSE")));

        mockMvc.perform(delete("/api/auth/users/{id}", userId)
                        .with(authentication(nurseAuth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenNonExistentUser_whenDeleteUser_thenReturns404() throws Exception {
        var userId = UUID.randomUUID();
        var adminId = UUID.randomUUID();
        var adminAuth = new UsernamePasswordAuthenticationToken(adminId, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        doThrow(new ResourceNotFoundException(ExceptionMessages.USER_NOT_FOUND)).when(userService).deleteUser(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/auth/users/{id}", userId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.USER_NOT_FOUND));
    }

    @Test
    void givenToBeDeletedUserIsSelf_whenDeleteUser_thenReturns403() throws Exception {
        var userId = UUID.randomUUID();
        var adminAuth = new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        doThrow(new SelfDeletionException()).when(userService).deleteUser(eq(userId), eq(userId));

        mockMvc.perform(delete("/api/auth/users/{id}", userId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(ExceptionMessages.SELF_DELETION));
    }

    @Test
    void givenNoAuthentication_whenDeleteUser_thenReturns403() throws Exception {
        var userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/auth/users/{id}", userId))
                .andExpect(status().isForbidden());
    }
}
