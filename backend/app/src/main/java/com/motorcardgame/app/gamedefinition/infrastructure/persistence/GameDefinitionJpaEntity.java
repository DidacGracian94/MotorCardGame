package com.motorcardgame.app.gamedefinition.infrastructure.persistence;

import com.motorcardgame.app.gamedefinition.domain.GameDefinitionVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "game_definitions")
public class GameDefinitionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameDefinitionVisibility visibility;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected GameDefinitionJpaEntity() {
        // requerido por JPA
    }

    public GameDefinitionJpaEntity(
            UUID id,
            UUID ownerId,
            String name,
            String slug,
            GameDefinitionVisibility visibility,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.slug = slug;
        this.visibility = visibility;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public GameDefinitionVisibility getVisibility() {
        return visibility;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
