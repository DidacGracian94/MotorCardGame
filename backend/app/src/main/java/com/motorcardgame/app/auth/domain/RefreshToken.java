package com.motorcardgame.app.auth.domain;

import java.time.Instant;
import java.util.UUID;

public final class RefreshToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private Instant revokedAt;
    private final Instant createdAt;

    private RefreshToken(
            UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant revokedAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
    }

    public static RefreshToken create(UUID userId, String tokenHash, Instant expiresAt) {
        return new RefreshToken(UUID.randomUUID(), userId, tokenHash, expiresAt, null, Instant.now());
    }

    public static RefreshToken reconstitute(
            UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant revokedAt, Instant createdAt) {
        return new RefreshToken(id, userId, tokenHash, expiresAt, revokedAt, createdAt);
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }

    public boolean isValid() {
        return revokedAt == null && expiresAt.isAfter(Instant.now());
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant revokedAt() {
        return revokedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshToken other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
