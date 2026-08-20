package com.motorcardgame.app.testsupport;

import com.motorcardgame.app.auth.application.TokenClaims;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockUserIdSecurityContextFactory implements WithSecurityContextFactory<WithMockUserId> {

    @Override
    public SecurityContext createSecurityContext(WithMockUserId annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        TokenClaims claims = new TokenClaims(UUID.randomUUID(), annotation.role());
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + annotation.role().name()));
        context.setAuthentication(new UsernamePasswordAuthenticationToken(claims, null, authorities));
        return context;
    }
}
