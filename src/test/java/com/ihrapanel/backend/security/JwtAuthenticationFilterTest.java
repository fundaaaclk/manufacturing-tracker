package com.ihrapanel.backend.security;


import com.ihrapanel.backend.tenant.TenantContext;
import com.ihrapanel.backend.user.Role;
import com.ihrapanel.backend.user.User;
import com.ihrapanel.backend.user.UserRepository;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(jwtService, userRepository);

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void shouldSetTenantContextForAuthenticatedRequestAndClearItAfterRequest()
            throws Exception {

        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        String token = "valid-token";

        User user = mock(User.class);

        when(user.isActive()).thenReturn(true);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId.toString());
        when(jwtService.extractCompanyId(token)).thenReturn(companyId.toString());
        when(jwtService.extractRole(token)).thenReturn(Role.OWNER.name());

        when(userRepository.findByIdAndCompanyId(userId, companyId))
                .thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        doAnswer(invocation -> {

            // Filter chain çalışırken tenant context set edilmiş olmalı.
            assertTrue(TenantContext.isSet());
            assertEquals(companyId, TenantContext.getCompanyId());

            return null;

        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        // Request bittikten sonra tenant context temizlenmiş olmalı.
        assertFalse(TenantContext.isSet());

        assertNotNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetTenantContextWhenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertFalse(TenantContext.isSet());

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldNotSetTenantContextWhenUserIsInactive()
            throws Exception {

        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        String token = "valid-token";

        User user = mock(User.class);

        when(user.isActive()).thenReturn(false);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId.toString());
        when(jwtService.extractCompanyId(token)).thenReturn(companyId.toString());
        when(jwtService.extractRole(token)).thenReturn(Role.OWNER.name());

        when(userRepository.findByIdAndCompanyId(userId, companyId))
                .thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertFalse(TenantContext.isSet());

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }
}