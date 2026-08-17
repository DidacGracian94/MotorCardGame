package com.motorcardgame.app.gamedefinition.instance.domain;

import java.time.Instant;
import java.util.UUID;

public final class GameInstance {

    private final UUID id;
    private final UUID gameDefinitionId;
    private final UUID gameDefinitionVersionId;
    private final String state;
    private final Instant createdAt;
    private final Instant endedAt;

    private GameInstance(
            UUID id,
            UUID gameDefinitionId,
            UUID gameDefinitionVersionId,
            String state,
            Instant createdAt,
            Instant endedAt) {
        this.id = id;
        this.gameDefinitionId = gameDefinitionId;
        this.gameDefinitionVersionId = gameDefinitionVersionId;
        this.state = state;
        this.createdAt = createdAt;
        this.endedAt = endedAt;
    }

    public static GameInstance create(UUID gameDefinitionId, UUID gameDefinitionVersionId, String state) {
        return new GameInstance(UUID.randomUUID(), gameDefinitionId, gameDefinitionVersionId, state, Instant.now(), null);
    }

    public GameInstance withState(String newState) {
        return new GameInstance(id, gameDefinitionId, gameDefinitionVersionId, newState, createdAt, endedAt);
    }

    public static GameInstance reconstitute(
            UUID id,
            UUID gameDefinitionId,
            UUID gameDefinitionVersionId,
            String state,
            Instant createdAt,
            Instant endedAt) {
        return new GameInstance(id, gameDefinitionId, gameDefinitionVersionId, state, createdAt, endedAt);
    }

    public UUID id() {
        return id;
    }

    public UUID gameDefinitionId() {
        return gameDefinitionId;
    }

    public UUID gameDefinitionVersionId() {
        return gameDefinitionVersionId;
    }

    public String state() {
        return state;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant endedAt() {
        return endedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameInstance other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
