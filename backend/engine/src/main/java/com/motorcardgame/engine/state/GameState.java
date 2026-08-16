package com.motorcardgame.engine.state;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Estado mutable en memoria de una partida en curso: jugadores, turno actual y zonas. No es
 * {@code GameInstance} — es el estado que una futura {@code GameInstance} envolverá junto con su
 * identidad y persistencia.
 *
 * <p>Las zonas compartidas ({@code SHARED}) y las zonas por jugador ({@code PER_PLAYER}) se
 * registran y buscan con pares de métodos distintos en vez de un único método parametrizado por
 * un flag de ownership — la distinción queda expresada en qué método se llama, comprobable en
 * compilación.
 */
public final class GameState {

    private final List<Player> players;
    private int currentPlayerIndex = 0;
    private final Map<String, Zone> sharedZones = new LinkedHashMap<>();
    private final Map<String, Map<PlayerId, Zone>> perPlayerZones = new LinkedHashMap<>();

    public GameState(List<Player> players) {
        this.players = List.copyOf(Objects.requireNonNull(players, "players"));
    }

    public List<Player> players() {
        return players;
    }

    public Player currentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public void registerSharedZone(String name, Zone zone) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(zone, "zone");
        if (sharedZones.containsKey(name)) {
            throw new IllegalStateException("Shared zone already registered: " + name);
        }
        sharedZones.put(name, zone);
    }

    public Zone sharedZone(String name) {
        Zone zone = sharedZones.get(name);
        if (zone == null) {
            throw new NoSuchElementException("Unknown shared zone: " + name);
        }
        return zone;
    }

    public void registerPlayerZone(PlayerId owner, String name, Zone zone) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(zone, "zone");
        Map<PlayerId, Zone> zonesByOwner = perPlayerZones.computeIfAbsent(name, key -> new LinkedHashMap<>());
        if (zonesByOwner.containsKey(owner)) {
            throw new IllegalStateException("Player zone already registered: " + name + " for " + owner);
        }
        zonesByOwner.put(owner, zone);
    }

    public Zone zoneOf(PlayerId owner, String name) {
        Map<PlayerId, Zone> zonesByOwner = perPlayerZones.get(name);
        Zone zone = zonesByOwner == null ? null : zonesByOwner.get(owner);
        if (zone == null) {
            throw new NoSuchElementException("Unknown player zone: " + name + " for " + owner);
        }
        return zone;
    }

    public Zone zoneOfCurrentPlayer(String name) {
        return zoneOf(currentPlayer().id(), name);
    }
}
