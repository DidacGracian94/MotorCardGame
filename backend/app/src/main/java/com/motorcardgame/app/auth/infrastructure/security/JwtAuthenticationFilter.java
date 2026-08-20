package com.motorcardgame.app.auth.infrastructure.security;

import com.motorcardgame.app.auth.application.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee {@code Authorization: Bearer <token>}, valida el JWT de acceso y, si es válido, deja
 * autenticada la request con el {@code userId} como principal. No autentica nada por sí mismo si
 * falta o es inválido — deja que las reglas de {@code SecurityConfig} devuelvan 401 para las rutas
 * que lo requieran.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            jwtService.validateAccessToken(token).ifPresent(userId -> setAuthenticatedUser(userId));
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthenticatedUser(UUID userId) {
        var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
