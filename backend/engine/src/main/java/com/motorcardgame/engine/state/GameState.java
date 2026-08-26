package com.motorcardgame.engine.state;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

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
    private int direction = 1;
    private final Map<String, Zone> sharedZones = new LinkedHashMap<>();
    private final Map<String, Map<PlayerId, Zone>> perPlayerZones = new LinkedHashMap<>();
    private final Map<String, Object> variables = new LinkedHashMap<>();
    private boolean ended;
    private final Set<PlayerId> winners = new LinkedHashSet<>();

    public GameState(List<Player> players) {
        this(players, 0, 1);
    }

    /**
     * Reconstruye un estado con el turno ya avanzado a un jugador concreto — lo usa
     * {@code GameStateSerializer} al deserializar un {@code GameState} persistido, donde el
     * índice de turno es parte de lo guardado, no algo que se recalcule avanzando turno a turno.
     */
    public GameState(List<Player> players, int currentPlayerIndex) {
        this(players, currentPlayerIndex, 1);
    }

    /**
     * Igual que {@link #GameState(List, int)} pero reconstruyendo también el sentido de turno
     * ({@code 1} hacia adelante, {@code -1} invertido) — lo usa {@code GameStateSerializer} para
     * que una partida con el sentido ya invertido (p.ej. tras jugar un "cambio de sentido") lo
     * conserve entre peticiones, en vez de volver siempre hacia adelante al deserializar.
     */
    public GameState(List<Player> players, int currentPlayerIndex, int direction) {
        this(players, currentPlayerIndex, direction, false, List.of());
    }

    /**
     * Igual que {@link #GameState(List, int, int)} pero reconstruyendo también si la partida ya
     * había terminado y quién la había ganado — lo usa {@code GameStateSerializer} para que una
     * partida ya cerrada (ver {@code DECLARE_WINNER}) conserve ese cierre entre peticiones, en vez
     * de "reabrirse" al deserializar.
     */
    public GameState(List<Player> players, int currentPlayerIndex, int direction, boolean ended, List<PlayerId> winners) {
        this.players = List.copyOf(Objects.requireNonNull(players, "players"));
        boolean outOfRange = currentPlayerIndex < 0 || currentPlayerIndex >= this.players.size();
        if (!this.players.isEmpty() && outOfRange) {
            throw new IndexOutOfBoundsException("currentPlayerIndex out of range: " + currentPlayerIndex);
        }
        if (direction != 1 && direction != -1) {
            throw new IllegalArgumentException("direction must be 1 or -1: " + direction);
        }
        this.currentPlayerIndex = currentPlayerIndex;
        this.direction = direction;
        this.ended = ended;
        this.winners.addAll(Objects.requireNonNull(winners, "winners"));
    }

    public List<Player> players() {
        return players;
    }

    public Player currentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int currentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Jugador al que le tocaría a continuación si se avanzara turno ahora mismo, sin mutar el
     * estado — lo usa el target {@code NEXT_PLAYER} para aplicar una acción (p.ej. robar cartas)
     * a esa persona sin necesidad de avanzar el turno primero.
     */
    public Player nextPlayer() {
        return players.get(Math.floorMod(currentPlayerIndex + direction, players.size()));
    }

    public int direction() {
        return direction;
    }

    /**
     * Invierte el sentido en el que {@link #advanceTurn()} recorre a los jugadores — lo dispara
     * la acción {@code REVERSE_DIRECTION} (p.ej. al jugar una carta de "cambio de sentido").
     */
    public void reverseDirection() {
        direction = -direction;
    }

    public boolean isCurrentPlayer(PlayerId playerId) {
        return currentPlayer().id().equals(playerId);
    }

    public void advanceTurn() {
        currentPlayerIndex = Math.floorMod(currentPlayerIndex + direction, players.size());
    }

    /**
     * Cede el turno directamente a {@code id}, a diferencia de {@link #advanceTurn()}, que solo
     * avanza secuencialmente — lo usa {@code SET_CURRENT_PLAYER} para dar el turno a un jugador
     * resuelto dinámicamente (p.ej. quien ganó una baza), no necesariamente el siguiente en orden
     * de asiento.
     */
    public void setCurrentPlayer(PlayerId id) {
        Objects.requireNonNull(id, "id");
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).id().equals(id)) {
                currentPlayerIndex = i;
                return;
            }
        }
        throw new NoSuchElementException("Unknown player: " + id);
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

    /**
     * Vista de solo lectura de las zonas compartidas registradas, indexadas por nombre. Existe
     * para que {@code GameStateSerializer} pueda enumerar qué zonas hay — sin esto no habría
     * forma de persistir el estado completo, solo de consultar una zona conocida de antemano.
     */
    public Map<String, Zone> sharedZones() {
        return Map.copyOf(sharedZones);
    }

    /**
     * Vista de solo lectura de las zonas por jugador registradas, indexadas por nombre y luego
     * por propietario. Mismo motivo que {@link #sharedZones()}.
     */
    public Map<String, Map<PlayerId, Zone>> perPlayerZones() {
        return Map.copyOf(perPlayerZones);
    }

    /**
     * Recuerda {@code value} bajo {@code name}, de forma persistente e independiente de dónde esté
     * ahora la carta (si alguna) que lo reveló — a diferencia de leer el atributo de la carta que
     * está en el tope de una zona, este valor no desaparece cuando esa carta se mueve a otra zona
     * (p.ej. el palo de triunfo, que sigue valiendo toda la mano aunque la carta que lo reveló
     * acabe en la mano de un jugador). Dispara la acción {@code REMEMBER_CARD_ATTRIBUTE}.
     */
    public void setVariable(String name, Object value) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(value, "value");
        variables.put(name, value);
    }

    /**
     * Devuelve {@code null} si {@code name} nunca se ha fijado — un estado normal (p.ej. antes de
     * que se ejecuten las reglas de {@code GAME_STARTED}), no un error.
     */
    public Object variable(String name) {
        return variables.get(name);
    }

    /**
     * Vista de solo lectura de las variables recordadas. Mismo motivo que {@link #sharedZones()}.
     */
    public Map<String, Object> variables() {
        return Map.copyOf(variables);
    }

    /**
     * Declara a {@code id} ganador y marca la partida como terminada — dispara la acción
     * {@code DECLARE_WINNER} sobre el jugador que resuelva el target de la regla (p.ej.
     * {@code CURRENT_PLAYER} al vaciar la mano). Añadir al mismo jugador dos veces no lo duplica;
     * una regla que resuelva a varios jugadores a la vez (p.ej. un empate) declara varios ganadores
     * sin que este método sepa nada de esa mecánica en concreto.
     */
    public void declareWinner(PlayerId id) {
        Objects.requireNonNull(id, "id");
        winners.add(id);
        ended = true;
    }

    public boolean isEnded() {
        return ended;
    }

    /**
     * Vista de solo lectura de los jugadores declarados ganadores, en el orden en que se
     * declararon. Vacía mientras la partida no ha terminado.
     */
    public List<PlayerId> winners() {
        return List.copyOf(winners);
    }
}
