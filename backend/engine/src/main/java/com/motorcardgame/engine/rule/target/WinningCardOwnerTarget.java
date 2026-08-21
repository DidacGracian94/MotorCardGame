package com.motorcardgame.engine.rule.target;

import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.rule.Target;
import com.motorcardgame.engine.rule.condition.Position;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Resuelve al jugador que jugó la carta ganadora de {@code zone}, comparando por
 * {@code rankAttribute} (numérico) dentro de un grupo de prioridad — genérico para cualquier
 * mecánica de "gana la carta más fuerte del grupo preferido; si no hay ninguna, gana la más fuerte
 * del grupo que abrió la ronda" (p.ej. triunfo vs. palo de salida en un juego de baza, sin que este
 * target sepa nada de Brisca ni de ningún juego concreto). Requiere que las cartas de {@code zone}
 * lleven grabado, en {@code ownerAttribute}, el id de quien las jugó (ver {@code MOVE_CARD}'s
 * {@code stampOwnerAs}).
 *
 * <p>Resolución:
 * <ol>
 *   <li>El valor preferido sale de {@code preferredGroupVariable} (si está presente — ver
 *       {@code REMEMBER_CARD_ATTRIBUTE}, un valor recordado que no depende de dónde esté ahora
 *       ninguna carta) o, si no, de {@code groupAttribute} de la carta en
 *       {@code preferredGroupPosition} de {@code preferredGroupZone} (si esa zona está vacía, o si
 *       la variable aún no se ha recordado, este nivel se salta).</li>
 *   <li>El grupo candidato son las cartas de {@code zone} cuyo {@code groupAttribute} coincide con
 *       ese valor; si ninguna coincide (o no había valor preferido), el grupo candidato pasa a ser
 *       las cartas cuyo {@code groupAttribute} coincide con el de la carta del fondo de
 *       {@code zone} — la primera jugada, ya que las cartas se apilan por el tope al jugarse.</li>
 *   <li>Gana, dentro del grupo candidato, la carta con mayor {@code rankAttribute}.</li>
 * </ol>
 */
public final class WinningCardOwnerTarget implements Target {

    private final ZoneRef zone;
    private final String rankAttribute;
    private final String groupAttribute;
    private final ZoneRef preferredGroupZone;
    private final Position preferredGroupPosition;
    private final String ownerAttribute;
    private final String preferredGroupVariable;

    public WinningCardOwnerTarget(
            ZoneRef zone,
            String rankAttribute,
            String groupAttribute,
            ZoneRef preferredGroupZone,
            Position preferredGroupPosition,
            String ownerAttribute) {
        this(zone, rankAttribute, groupAttribute, preferredGroupZone, preferredGroupPosition, ownerAttribute, null);
    }

    /**
     * @param preferredGroupZone     zona de la que leer el valor preferido en vivo — puede ser
     *                               {@code null} si se da {@code preferredGroupVariable}.
     * @param preferredGroupVariable nombre de una variable recordada (ver
     *                               {@code REMEMBER_CARD_ATTRIBUTE}) de la que leer el valor
     *                               preferido en vez de una zona — puede ser {@code null} si se da
     *                               {@code preferredGroupZone}. Al menos uno de los dos es
     *                               obligatorio.
     */
    public WinningCardOwnerTarget(
            ZoneRef zone,
            String rankAttribute,
            String groupAttribute,
            ZoneRef preferredGroupZone,
            Position preferredGroupPosition,
            String ownerAttribute,
            String preferredGroupVariable) {
        this.zone = Objects.requireNonNull(zone, "zone");
        this.rankAttribute = Objects.requireNonNull(rankAttribute, "rankAttribute");
        this.groupAttribute = Objects.requireNonNull(groupAttribute, "groupAttribute");
        if (preferredGroupZone == null && preferredGroupVariable == null) {
            throw new IllegalArgumentException(
                    "WinningCardOwnerTarget requires either preferredGroupZone or preferredGroupVariable");
        }
        this.preferredGroupZone = preferredGroupZone;
        this.preferredGroupPosition = Objects.requireNonNull(preferredGroupPosition, "preferredGroupPosition");
        this.ownerAttribute = Objects.requireNonNull(ownerAttribute, "ownerAttribute");
        this.preferredGroupVariable = preferredGroupVariable;
    }

    @Override
    public List<Object> resolve(RuleContext context) {
        GameState gameState = context.gameState();
        Player currentPlayer = gameState.currentPlayer();
        LinearZone pool = (LinearZone) zone.resolve(gameState, currentPlayer);

        Object preferredValue = preferredGroupVariable != null
                ? gameState.variable(preferredGroupVariable)
                : preferredValueFromZone(gameState, currentPlayer);
        List<Card> candidates = preferredValue == null ? List.of() : matching(pool, preferredValue);

        if (candidates.isEmpty()) {
            Object ledValue = pool.peekBottom().attribute(groupAttribute);
            candidates = matching(pool, ledValue);
        }

        Card winner = candidates.stream()
                .max(Comparator.comparingInt(this::rank))
                .orElseThrow();

        Object ownerId = winner.attribute(ownerAttribute);
        return List.of(gameState.players().stream()
                .filter(player -> player.id().value().equals(ownerId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Unknown player id in \"" + ownerAttribute + "\": " + ownerId)));
    }

    private Object preferredValueFromZone(GameState gameState, Player currentPlayer) {
        LinearZone preferredZone = (LinearZone) preferredGroupZone.resolve(gameState, currentPlayer);
        return preferredZone.isEmpty() ? null : preferredCard(preferredZone).attribute(groupAttribute);
    }

    private List<Card> matching(LinearZone pool, Object groupValue) {
        return pool.cardsView().stream()
                .filter(card -> Objects.equals(groupValue, card.attribute(groupAttribute)))
                .toList();
    }

    private Card preferredCard(LinearZone preferredZone) {
        return preferredGroupPosition == Position.TOP ? preferredZone.peekTop() : preferredZone.peekBottom();
    }

    private int rank(Card card) {
        return (Integer) card.attribute(rankAttribute);
    }
}
