package com.motorcardgame.app.gamedefinition.version.domain;

import java.time.Instant;
import java.util.UUID;

public final class GameDefinitionVersion {

    private final UUID id;
    private final UUID gameDefinitionId;
    private final int versionNumber;
    private final String config;
    private final Instant createdAt;
    private final Instant publishedAt;

    private GameDefinitionVersion(
            UUID id, UUID gameDefinitionId, int versionNumber, String config, Instant createdAt, Instant publishedAt) {
        this.id = id;
        this.gameDefinitionId = gameDefinitionId;
        this.versionNumber = versionNumber;
        this.config = config;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
    }

    public static GameDefinitionVersion publish(UUID gameDefinitionId, int versionNumber, String config) {
        Instant now = Instant.now();
        return new GameDefinitionVersion(UUID.randomUUID(), gameDefinitionId, versionNumber, config, now, now);
    }

    public static GameDefinitionVersion reconstitute(
            UUID id, UUID gameDefinitionId, int versionNumber, String config, Instant createdAt, Instant publishedAt) {
        return new GameDefinitionVersion(id, gameDefinitionId, versionNumber, config, createdAt, publishedAt);
    }

    public UUID id() {
        return id;
    }

    public UUID gameDefinitionId() {
        return gameDefinitionId;
    }

    public int versionNumber() {
        return versionNumber;
    }

    public String config() {
        return config;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant publishedAt() {
        return publishedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameDefinitionVersion other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
