package com.motorcardgame.app.gamedefinition.version.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "game_definition_versions")
public class GameDefinitionVersionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "game_definition_id", nullable = false)
    private UUID gameDefinitionId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String config;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected GameDefinitionVersionJpaEntity() {
        // requerido por JPA
    }

    public GameDefinitionVersionJpaEntity(
            UUID id, UUID gameDefinitionId, int versionNumber, String config, Instant createdAt, Instant publishedAt) {
        this.id = id;
        this.gameDefinitionId = gameDefinitionId;
        this.versionNumber = versionNumber;
        this.config = config;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGameDefinitionId() {
        return gameDefinitionId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getConfig() {
        return config;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
