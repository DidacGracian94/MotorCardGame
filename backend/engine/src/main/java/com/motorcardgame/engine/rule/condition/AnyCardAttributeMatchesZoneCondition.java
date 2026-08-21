package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Objects;

/**
 * Comprueba si ALGUNA carta de {@code zone} tiene, en {@code attribute}, el mismo valor que la
 * carta del tope o fondo ({@code referencePosition}) de {@code referenceZone} — a diferencia de
 * {@link CardAttributeMatchesZoneCondition}, que compara UNA carta concreta (la referenciada por
 * el evento) contra otra, esta condición barre {@code zone} entera. Es la pieza que falta para
 * expresar "sigo palo si tengo alguna carta de ese palo" en un juego de baza: se usa negada
 * (dentro de un {@code NOT}) como "vía de escape" — "no tengo ninguna carta que siga el palo, así
 * que puedo jugar cualquier cosa". Si {@code referenceZone} está vacía no hay valor contra el que
 * comparar, así que no se lanza: se considera que ninguna carta coincide (falso), igual que
 * {@link com.motorcardgame.engine.rule.target.WinningCardOwnerTarget} salta un nivel de prioridad
 * cuando su zona preferida está vacía en vez de lanzar.
 */
public final class AnyCardAttributeMatchesZoneCondition implements Condition {

    private final ZoneRef zone;
    private final String attribute;
    private final ZoneRef referenceZone;
    private final Position referencePosition;

    public AnyCardAttributeMatchesZoneCondition(
            ZoneRef zone, String attribute, ZoneRef referenceZone, Position referencePosition) {
        this.zone = Objects.requireNonNull(zone, "zone");
        this.attribute = Objects.requireNonNull(attribute, "attribute");
        this.referenceZone = Objects.requireNonNull(referenceZone, "referenceZone");
        this.referencePosition = Objects.requireNonNull(referencePosition, "referencePosition");
    }

    @Override
    public boolean evaluate(RuleContext context) {
        Player currentPlayer = context.gameState().currentPlayer();
        LinearZone pool = (LinearZone) zone.resolve(context.gameState(), currentPlayer);
        LinearZone reference = (LinearZone) referenceZone.resolve(context.gameState(), currentPlayer);
        if (reference.isEmpty()) {
            return false;
        }
        Object referenceValue = (referencePosition == Position.TOP ? reference.peekTop() : reference.peekBottom())
                .attribute(attribute);
        return pool.cardsView().stream()
                .anyMatch(card -> Objects.equals(referenceValue, card.attribute(attribute)));
    }
}
