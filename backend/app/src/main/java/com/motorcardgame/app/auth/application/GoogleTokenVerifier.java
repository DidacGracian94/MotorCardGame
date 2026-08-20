package com.motorcardgame.app.auth.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Verifica el {@code id_token} que manda el frontend tras un login con Google Identity Services
 * llamando al endpoint público {@code tokeninfo} de Google, que valida la firma (JWKS de Google)
 * y devuelve el payload decodificado si es válido. Evita reimplementar la verificación JWKS a
 * mano y no añade ninguna dependencia nueva (usa el {@code RestClient} que ya trae
 * spring-boot-starter-web).
 */
@Service
public class GoogleTokenVerifier {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={idToken}";

    private final RestClient restClient;
    private final String googleClientId;

    public GoogleTokenVerifier(@Value("${google.client-id}") String googleClientId) {
        this.restClient = RestClient.create();
        this.googleClientId = googleClientId;
    }

    public GoogleIdentity verify(String idToken) {
        TokenInfoResponse response;
        try {
            response = restClient.get()
                    .uri(TOKENINFO_URL, idToken)
                    .retrieve()
                    .body(TokenInfoResponse.class);
        } catch (RestClientException e) {
            throw new InvalidGoogleTokenException(e);
        }
        if (response == null
                || response.aud() == null
                || !response.aud().equals(googleClientId)
                || !"true".equals(response.emailVerified())
                || response.sub() == null
                || response.email() == null) {
            throw new InvalidGoogleTokenException();
        }
        String displayName = response.name() != null ? response.name() : response.email();
        return new GoogleIdentity(response.sub(), response.email(), displayName);
    }

    public record GoogleIdentity(String subject, String email, String displayName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenInfoResponse(
            String aud,
            String sub,
            String email,
            @JsonProperty("email_verified") String emailVerified,
            String name) {
    }
}
