package com.motorcardgame.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * A propósito NO usa MockMvc: MockMvc no reproduce el reenvío interno de un contenedor real de
 * servlets a {@code /error} tras un {@code sendError()} (lo que hace Tomcat para un 403/500), así
 * que un test con MockMvc no puede detectar la regresión real que motiva este test — con puerto
 * aleatorio y {@code TestRestTemplate} sí se ejercita ese reenvío. Bug real que reprodujo esto: sin
 * {@code /error} en la lista permitAll de {@link SecurityConfig}, ese segundo paso por la cadena de
 * filtros llegaba con {@code SecurityContext} anónimo, {@code .anyRequest().authenticated()} lo
 * rechazaba de nuevo, y el {@code authenticationEntryPoint} pisaba el 403 original con un 401 —
 * cualquier fallo de autorización (no solo de autenticación) le llegaba al cliente como 401.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityConfigIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void adminOnlyEndpoint_returns403NotClobberedTo401_whenCallerIsAuthenticatedButNotAdmin() {
        String accessToken = registerAndGetAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/admin/users", org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void protectedEndpoint_returns401_whenCallerHasNoToken() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/game-definitions", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String registerAndGetAccessToken() {
        Map<String, Object> request = Map.of(
                "email", "security-config-" + UUID.randomUUID() + "@example.com",
                "password", "correct-horse-battery-staple",
                "displayName", "Security Config Test User");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response =
                restTemplate.postForEntity("/api/auth/register", new HttpEntity<>(request, headers), Map.class);
        return (String) response.getBody().get("accessToken");
    }
}
