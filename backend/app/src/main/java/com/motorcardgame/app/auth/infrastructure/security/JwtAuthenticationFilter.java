package com.motorcardgame.app.auth.infrastructure.security;

import com.motorcardgame.app.auth.application.JwtService;
import com.motorcardgame.app.auth.application.TokenClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee {@code Authorization: Bearer <token>}, valida el JWT de acceso y, si es válido, deja
 * autenticada la request con {@link TokenClaims} (userId + role) como principal — así los
 * controllers pueden aplicar autorización por dueño/rol sin otra consulta a base de datos. No
 * autentica nada por sí mismo si falta o es inválido — deja que las reglas de
 * {@code SecurityConfig} devuelvan 401 para las rutas que lo requieran.
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
            jwtService.validateAccessToken(token).ifPresent(this::setAuthenticatedUser);
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthenticatedUser(TokenClaims claims) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + claims.role().name()));
        var authentication = new UsernamePasswordAuthenticationToken(claims, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
