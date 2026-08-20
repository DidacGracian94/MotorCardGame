package com.motorcardgame.app.testsupport;

import com.motorcardgame.app.auth.domain.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * Autentica los tests de MockMvc con un usuario ficticio (UUID aleatorio) sin pasar por un JWT
 * real — para tests centrados en lógica de negocio (GameDefinition/GameInstance) que no están
 * probando el mecanismo de auth en sí (eso lo cubre {@code AuthControllerIntegrationTest}).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@WithSecurityContext(factory = WithMockUserIdSecurityContextFactory.class)
public @interface WithMockUserId {

    Role role() default Role.USER;
}
