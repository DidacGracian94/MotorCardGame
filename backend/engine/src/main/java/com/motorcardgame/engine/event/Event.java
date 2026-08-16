package com.motorcardgame.engine.event;

import java.util.Map;
import java.util.Objects;

/**
 * Un evento ocurrido durante una partida (p.ej. "CARD_PLAYED", "TURN_STARTED") que puede
 * disparar la evaluación de reglas. El tipo es una cadena libre, igual que los nombres de
 * capacidad del engine — no se valida contra ningún registry: una regla cuyo tipo de evento
 * nunca coincide con ninguno disparado es un problema de configuración, no del motor.
 */
public final class Event {

    private final String type;
    private final Map<String, Object> payload;

    public static Event of(String type) {
        return new Event(type, Map.of());
    }

    public Event(String type, Map<String, Object> payload) {
        this.type = Objects.requireNonNull(type, "type");
        this.payload = Map.copyOf(Objects.requireNonNull(payload, "payload"));
    }

    public String type() {
        return type;
    }

    public Object payload(String key) {
        return payload.get(key);
    }
}
