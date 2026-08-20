package com.motorcardgame.app.auth.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * Emite y valida los JWT de acceso propios (RS256) y gestiona el valor opaco de los refresh
 * tokens (no son JWT: son un secreto aleatorio cuyo hash se guarda en {@code refresh_tokens} para
 * poder revocarlos/rotarlos, algo que un JWT autocontenido no permite sin una lista de revocación
 * aparte).
 */
@Service
public class JwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtService(
            ResourceLoader resourceLoader,
            @Value("${jwt.private-key-path}") String privateKeyPath,
            @Value("${jwt.public-key-path}") String publicKeyPath,
            @Value("${jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes,
            @Value("${jwt.refresh-token-ttl-days}") long refreshTokenTtlDays) {
        this.privateKey = loadPrivateKey(resourceLoader, privateKeyPath);
        this.publicKey = loadPublicKey(resourceLoader, publicKeyPath);
        this.accessTokenTtl = Duration.ofMinutes(accessTokenTtlMinutes);
        this.refreshTokenTtl = Duration.ofDays(refreshTokenTtlDays);
    }

    public String issueAccessToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Optional<UUID> validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
            return Optional.of(UUID.fromString(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String generateRefreshTokenValue() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashRefreshToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public Duration refreshTokenTtl() {
        return refreshTokenTtl;
    }

    private static PrivateKey loadPrivateKey(ResourceLoader resourceLoader, String path) {
        try {
            byte[] decoded = readPemBytes(resourceLoader, path);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo cargar la clave privada JWT desde " + path, e);
        }
    }

    private static PublicKey loadPublicKey(ResourceLoader resourceLoader, String path) {
        try {
            byte[] decoded = readPemBytes(resourceLoader, path);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo cargar la clave pública JWT desde " + path, e);
        }
    }

    private static byte[] readPemBytes(ResourceLoader resourceLoader, String path) throws IOException {
        Resource resource = resourceLoader.getResource(path);
        String pem;
        try (InputStream in = resource.getInputStream()) {
            pem = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        String base64 = pem
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
