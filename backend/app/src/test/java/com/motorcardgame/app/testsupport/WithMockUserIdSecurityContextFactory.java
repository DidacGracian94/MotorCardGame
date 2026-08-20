package com.motorcardgame.app.testsupport;

import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockUserIdSecurityContextFactory implements WithSecurityContextFactory<WithMockUserId> {

    @Override
    public SecurityContext createSecurityContext(WithMockUserId annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(UUID.randomUUID(), null, List.of()));
        return context;
    }
}
