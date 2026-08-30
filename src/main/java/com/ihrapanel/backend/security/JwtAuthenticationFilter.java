package com.ihrapanel.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.ihrapanel.backend.user.Role;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Authorization header yoksa veya Bearer ile başlamıyorsa
        // JWT kontrolü yapmadan devam ederiz.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " kısmını çıkarıp sadece tokenı alıyoruz.
        String token = authHeader.substring(7);

        if (jwtService.isTokenValid(token)) {

            String userId = jwtService.extractUserId(token);
            String companyId = jwtService.extractCompanyId(token);
            String role = jwtService.extractRole(token);

        AuthenticatedUser principal =
                new AuthenticatedUser(
                  UUID.fromString(userId),
                  UUID.fromString(companyId),
                   Role.valueOf(role)
               );

         SimpleGrantedAuthority authority =
              new SimpleGrantedAuthority("ROLE_" + role);

     UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(authority)
        );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}