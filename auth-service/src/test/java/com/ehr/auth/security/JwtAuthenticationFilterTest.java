package com.ehr.auth.security;

import static com.ehr.auth.utils.UserServiceTestUtils.user;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ehr.auth.model.enums.UserRole;
import com.ehr.auth.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private HttpServletResponse servletResponse;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContextHolder securityContextHolder;

    @Mock
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, userRepository, objectMapper);
    }
    
    @Test
    void givenValidRequestAndUser_WhenDoFilterInternal_ThenDoFilterIsCalled() throws ServletException, IOException {
        var validToken = "valid-token";
        var userId = UUID.randomUUID();
        var testUser = user("testuser", UserRole.COORDINATOR);
        var grantedAuthorityRole = new SimpleGrantedAuthority("ROLE_" + testUser.getRole().name());

        when(servletRequest.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        jwtAuthenticationFilter.doFilterInternal(servletRequest, servletResponse, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(auth);
        assertTrue(auth.getAuthorities().contains(grantedAuthorityRole));
        assertEquals(userId, auth.getPrincipal());
        verify(filterChain, times(1)).doFilter(eq(servletRequest), eq(servletResponse));
    }

    @Test
    void givenInvalidRequest_WhenDoFilterInternal_ThenDoFilterIsCalled() throws ServletException, IOException {
        var invalidToken = "invalid-token";

        when(servletRequest.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);
        
        jwtAuthenticationFilter.doFilterInternal(servletRequest, servletResponse, filterChain);

        verify(filterChain, times(1)).doFilter(eq(servletRequest), eq(servletResponse));
    }

    @Test
    void givenNullRequest_WhenDoFilterInternal_ThenDoFilterIsCalled() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(servletRequest, servletResponse, filterChain);

        verify(filterChain, times(1)).doFilter(eq(servletRequest), eq(servletResponse));
    }

    @Test
    void givenValidRequestAndNoUser_WhenDoFilterInternal_ThenWriteErrorToResponse() throws ServletException, IOException {
        var validToken = "valid-token";
        var userId = UUID.randomUUID();

        when(servletRequest.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(servletResponse.getWriter()).thenReturn(printWriter);
        
        jwtAuthenticationFilter.doFilterInternal(servletRequest, servletResponse, filterChain);

        verify(servletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(servletResponse).setContentType("application/json");
        verifyNoInteractions(filterChain);
    }
}
