package com.motorcardgame.app.gamedefinition.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class GameDefinition {

    private final UUID id;
    private final UUID ownerId;
    private String name;
    private final String slug;
    private final Instant createdAt;
    private Instant updatedAt;

    private GameDefinition(UUID id, UUID ownerId, String name, String slug, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.slug = slug;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GameDefinition create(UUID ownerId, String name, String slug) {
        Instant now = Instant.now();
        return new GameDefinition(UUID.randomUUID(), ownerId, name, slug, now, now);
    }

    public static GameDefinition reconstitute(
            UUID id, UUID ownerId, String name, String slug, Instant createdAt, Instant updatedAt) {
        return new GameDefinition(id, ownerId, name, slug, createdAt, updatedAt);
    }

    public void rename(String newName) {
        this.name = Objects.requireNonNull(newName, "newName");
        this.updatedAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public String name() {
        return name;
    }

    public String slug() {
        return slug;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameDefinition other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
