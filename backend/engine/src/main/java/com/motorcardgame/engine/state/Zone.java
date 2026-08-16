package com.motorcardgame.engine.state;

/**
 * Una zona de cartas dentro de una partida (mazo, mano, pila de descarte, ...). Marcador sellado
 * por topología: hoy solo existe la pila lineal ({@link LinearZone}), que cubre todo lo que
 * necesita el roadmap actual (UNO/Brisca/Escoba). Una topología espacial/grid se añadiría como
 * un nuevo permitido — ver nota pendiente en CLAUDE.md sobre el modelo de Zone.
 *
 * <p>El ownership (SHARED vs PER_PLAYER) no es una propiedad de la zona en sí, sino de cómo
 * {@link GameState} la indexa — ver {@link GameState#registerSharedZone} /
 * {@link GameState#registerPlayerZone}.
 */
public sealed interface Zone permits LinearZone {
}
