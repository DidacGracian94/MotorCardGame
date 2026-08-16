package com.motorcardgame.app.gamedefinition.instance.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "game_instances")
public class GameInstanceJpaEntity {

    @Id
    private UUID id;

    @Column(name = "game_definition_id", nullable = false)
    private UUID gameDefinitionId;

    @Column(name = "game_definition_version_id", nullable = false)
    private UUID gameDefinitionVersionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String state;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    protected GameInstanceJpaEntity() {
        // requerido por JPA
    }

    public GameInstanceJpaEntity(
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

    public UUID getId() {
        return id;
    }

    public UUID getGameDefinitionId() {
        return gameDefinitionId;
    }

    public UUID getGameDefinitionVersionId() {
        return gameDefinitionVersionId;
    }

    public String getState() {
        return state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }
}
