package com.ihrapanel.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.ihrapanel.backend.user.Role;
import com.ihrapanel.backend.user.UserRepository;
import com.ihrapanel.backend.tenant.TenantContext;

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
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
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

    String token = authHeader.substring(7);

    if (!jwtService.isTokenValid(token)) {
        filterChain.doFilter(request, response);
        return;
    }

    String userId = jwtService.extractUserId(token);
    String companyId = jwtService.extractCompanyId(token);
    String role = jwtService.extractRole(token);

    UUID userUuid = UUID.fromString(userId);
    UUID companyUuid = UUID.fromString(companyId);

    var user = userRepository.findByIdAndCompanyId(
            userUuid,
            companyUuid
    ).orElse(null);

    // User yoksa veya inactive ise(Companynın aktifliğede bakılır) JWT ile authentication yapma.
  if (user == null
        || !user.isActive()
        || !user.getCompany().isActive()) {

    filterChain.doFilter(request, response);
    return;
}

    AuthenticatedUser principal =
            new AuthenticatedUser(
                    userUuid,
                    companyUuid,
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

    // Authentication başarılıysa tenant bilgisini request boyunca taşıyoruz.
    TenantContext.setCompanyId(companyUuid);

    try {
        filterChain.doFilter(request, response);
    } finally {
        // Thread tekrar kullanılabileceği için tenant bilgisini mutlaka siliyoruz.
        TenantContext.clear();
    }
}
}