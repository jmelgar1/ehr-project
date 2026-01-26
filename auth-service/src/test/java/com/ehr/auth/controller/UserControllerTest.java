package com.ehr.auth.controller;

import static com.ehr.auth.constants.ApiPaths.USERS_BY_ID_API_PATH;

import com.ehr.auth.config.SecurityConfig;
import com.ehr.auth.exception.GlobalExceptionHandler;
import com.ehr.auth.exception.ResourceNotFoundException;
import com.ehr.auth.repository.UserRepository;
import com.ehr.auth.security.JwtTokenProvider;
import com.ehr.auth.service.AuthService;
import com.ehr.auth.service.PermissionResolverService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PermissionResolverService permissionResolverService;

    @Test
    void givenAdminUser_whenDeleteUser_thenReturns204() throws Exception {
        var userId = UUID.randomUUID();
        var adminId = UUID.randomUUID();
        var adminAuth = new UsernamePasswordAuthenticationToken(
                adminId, null, List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("PERMISSION_USER:DELETE")));
        doNothing().when(authService).deleteUser(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete(USERS_BY_ID_API_PATH, userId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenNonAdminUser_whenDeleteUser_thenReturns403() throws Exception {
        var userId = UUID.randomUUID();
        var nurseId = UUID.randomUUID();
        var nurseAuth = new UsernamePasswordAuthenticationToken(
                nurseId, null, List.of(new SimpleGrantedAuthority("ROLE_NURSE")));

        mockMvc.perform(delete(USERS_BY_ID_API_PATH, userId)
                        .with(authentication(nurseAuth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenNonExistentUser_whenDeleteUser_thenReturns404() throws Exception {
        var userId = UUID.randomUUID();
        var adminId = UUID.randomUUID();
        var adminAuth = new UsernamePasswordAuthenticationToken(
                adminId, null, List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("PERMISSION_USER:DELETE")));
        doThrow(new ResourceNotFoundException("User not found")).when(authService).deleteUser(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete(USERS_BY_ID_API_PATH, userId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void givenNoAuthentication_whenDeleteUser_thenReturns403() throws Exception {
        var userId = UUID.randomUUID();

        mockMvc.perform(delete(USERS_BY_ID_API_PATH, userId))
                .andExpect(status().isForbidden());
    }
}
