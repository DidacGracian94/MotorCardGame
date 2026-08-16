package com.motorcardgame.engine.exception;

/**
 * Se lanza al parsear una {@code GameDefinitionVersion.config} que no es JSON válido, le falta un
 * campo obligatorio, o referencia una capacidad no registrada. Indica un error en los datos de
 * configuración del juego, no un bug del motor.
 */
public final class InvalidGameDefinitionException extends RuntimeException {

    public InvalidGameDefinitionException(String message) {
        super(message);
    }

    public InvalidGameDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
