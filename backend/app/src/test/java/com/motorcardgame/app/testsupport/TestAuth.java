package com.motorcardgame.app.testsupport;

import com.motorcardgame.app.auth.application.TokenClaims;
import com.motorcardgame.app.auth.domain.Role;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Autentica una request de MockMvc como un usuario concreto (id + rol elegidos) — a diferencia de
 * {@link WithMockUserId}, que fija un único usuario ficticio para todo el método de test, esto
 * permite dentro del mismo test simular "el dueño" y "otro usuario distinto" para probar
 * autorización (own vs. ajeno, admin vs. normal).
 */
public final class TestAuth {

    private TestAuth() {
    }

    public static RequestPostProcessor as(UUID userId, Role role) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        var authentication = new UsernamePasswordAuthenticationToken(new TokenClaims(userId, role), null, authorities);
        return SecurityMockMvcRequestPostProcessors.authentication(authentication);
    }
}
