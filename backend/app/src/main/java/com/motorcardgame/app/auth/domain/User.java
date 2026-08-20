package com.motorcardgame.app.auth.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class User {

    private final UUID id;
    private final String email;
    private String passwordHash;
    private String googleSubject;
    private String displayName;
    private Role role;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(
            UUID id,
            String email,
            String passwordHash,
            String googleSubject,
            String displayName,
            Role role,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.googleSubject = googleSubject;
        this.displayName = displayName;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User registerWithPassword(String email, String passwordHash, String displayName) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), email, passwordHash, null, displayName, Role.USER, now, now);
    }

    public static User registerWithGoogle(String email, String googleSubject, String displayName) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), email, null, googleSubject, displayName, Role.USER, now, now);
    }

    public static User reconstitute(
            UUID id,
            String email,
            String passwordHash,
            String googleSubject,
            String displayName,
            Role role,
            Instant createdAt,
            Instant updatedAt) {
        return new User(id, email, passwordHash, googleSubject, displayName, role, createdAt, updatedAt);
    }

    public void linkGoogleSubject(String googleSubject) {
        this.googleSubject = Objects.requireNonNull(googleSubject, "googleSubject");
        this.updatedAt = Instant.now();
    }

    public void changeRole(Role newRole) {
        this.role = Objects.requireNonNull(newRole, "newRole");
        this.updatedAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public String googleSubject() {
        return googleSubject;
    }

    public String displayName() {
        return displayName;
    }

    public Role role() {
        return role;
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
        if (!(o instanceof User other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
