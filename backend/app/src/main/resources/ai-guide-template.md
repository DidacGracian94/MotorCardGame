# Guía para generar una GameDefinition de MotorCardGame

Vas a generar el JSON de configuración (`config`) de un juego de cartas para MotorCardGame, un
motor genérico de juegos de cartas. Este JSON se interpreta con capacidades reutilizables, nunca
con código. Sigue esta guía al pie de la letra: usa exactamente los nombres de campo y de
capacidad que aparecen aquí (son sensibles a mayúsculas), no inventes campos ni capacidades que no
estén en la lista de la sección "Capacidades disponibles" al final de este documento.

## Modelo de una GameDefinition

El JSON raíz (`config`) tiene estas claves:

- `"zones"` (obligatorio, array): las zonas del juego (mazo, mano, mesa, descarte, ...). Cada zona:
  `{ "name": "deck", "ownership": "SHARED" | "PER_PLAYER", "shuffle": true|false (opcional),
  "visibility": "PUBLIC"|"HIDDEN"|"OWNER_ONLY"|"ALL_BUT_OWNER" (opcional) }`.
  - `ownership: "SHARED"`: una única instancia de la zona, compartida por todos los jugadores
    (p.ej. el mazo o la mesa).
  - `ownership: "PER_PLAYER"`: cada jugador tiene su propia instancia de la zona (p.ej. la mano).
  - `visibility` por defecto: `PUBLIC` para zonas `SHARED`, `OWNER_ONLY` para zonas `PER_PLAYER`.
  - Todas las zonas son pilas lineales (tienen tope y fondo; no hay posiciones de tablero).
- `"cards"` (opcional, array): plantillas que pueblan zonas `SHARED` al empezar la partida. Cada
  entrada: `{ "id": "red-1", "zone": "deck", "count": 3 (opcional, por defecto 1),
  "attributes": { "color": "RED", "value": 5 }, "rules": [ ... ] (opcional) }`. Los `attributes`
  son pares clave/valor opacos al motor (texto, número o booleano) — decides tú qué atributos
  necesita tu juego (color, palo, valor, fuerza, puntos...) y las capacidades los leen por nombre.
  `rules` dentro de una carta son reglas ancladas a esa carta (se disparan igual que las globales).
- `"rules"` (obligatorio, array): la lógica del juego. Cada regla:
  `{ "event": "NOMBRE_EVENTO", "condition": { ... }, "target": { ... }, "action": { ... } }`.
  - `event`: el nombre del evento que dispara la regla. `"GAME_STARTED"` está siempre disponible
    (lo dispara el motor al crear la partida); cualquier otro nombre debe declararse en
    `"playerActions"` para poder usarse.
  - `condition`: un nodo `{ "type": "NOMBRE_CONDICION", ...campos... }` de la lista de condiciones
    disponibles. Si no hay condición, usa `{ "type": "AND", "conditions": [] }` (se cumple siempre).
  - `target`: a quién/qué se aplica la acción — un nodo `{ "type": "NOMBRE_TARGET", ...campos... }`.
  - `action`: qué ocurre — un nodo `{ "type": "NOMBRE_ACCION", ...campos... }`.
  - Una zona referenciada dentro de `condition`/`target`/`action` se escribe como
    `{ "name": "deck", "ownership": "SHARED" }` (o `"PER_PLAYER"`), no solo el nombre.
- `"playerActions"` (opcional, array de strings): nombres de evento que un jugador puede disparar
  desde la UI (p.ej. `"PLAY_CARD"`, `"DRAW_CARD"`). Si una regla referencia un evento no declarado
  aquí (ni `"GAME_STARTED"`), la definición no es válida.

## Lo que este motor NO tiene todavía (no lo inventes)

- No hay concepto explícito de "fases de turno" — el turno se controla con las acciones
  `NEXT_PLAYER` / `SET_CURRENT_PLAYER` / `REVERSE_DIRECTION` dentro de reglas normales.
- No hay condición de "fin de partida" ni "jugador ganador" como capacidad dedicada. Se puede
  llevar puntuación con `ADD_POINTS`, pero detectar el final y declarar un ganador no es una
  capacidad del motor hoy.
- No hay zonas con topología espacial/de tablero (todas son pilas lineales: tope y fondo).
- Nunca uses `"script"`, `"code"` ni ningún campo con código ejecutable — todo se expresa con las
  capacidades de la lista de más abajo.

## Ejemplo completo: resolución de una baza (genérico, válido para cualquier juego de baza)

Dos zonas SHARED (`mesa`, `triunfo`), una PER_PLAYER (`hand`) y otra PER_PLAYER (`bazas`) para las
cartas ganadas. Cuando un jugador juega una carta (evento `PLAY_CARD`), se mueve a la mesa; si ya
hay 2 cartas en la mesa, se resuelve quién gana la baza (la carta más fuerte del palo de triunfo,
o si nadie jugó triunfo, del palo que abrió la ronda), se le suman los puntos, se le cede el turno
y se recogen las cartas jugadas.

```json
{
  "zones": [
    { "name": "hand", "ownership": "PER_PLAYER" },
    { "name": "mesa", "ownership": "SHARED" },
    { "name": "triunfo", "ownership": "SHARED" },
    { "name": "bazas", "ownership": "PER_PLAYER" }
  ],
  "playerActions": ["PLAY_CARD"],
  "rules": [
    {
      "event": "PLAY_CARD",
      "condition": { "type": "AND", "conditions": [] },
      "target": { "type": "CURRENT_PLAYER" },
      "action": {
        "type": "MOVE_CARD",
        "from": { "name": "hand", "ownership": "PER_PLAYER" },
        "to": { "name": "mesa", "ownership": "SHARED" },
        "stampOwnerAs": "playedBy"
      }
    },
    {
      "event": "PLAY_CARD",
      "condition": {
        "type": "NOT",
        "condition": { "type": "ZONE_CARD_COUNT_EQUALS", "zone": { "name": "mesa", "ownership": "SHARED" }, "count": 2 }
      },
      "target": { "type": "CURRENT_PLAYER" },
      "action": { "type": "NEXT_PLAYER" }
    },
    {
      "event": "PLAY_CARD",
      "condition": { "type": "ZONE_CARD_COUNT_EQUALS", "zone": { "name": "mesa", "ownership": "SHARED" }, "count": 2 },
      "target": {
        "type": "WINNING_CARD_OWNER",
        "zone": { "name": "mesa", "ownership": "SHARED" },
        "rankAttribute": "strength",
        "groupAttribute": "suit",
        "preferredGroupZone": { "name": "triunfo", "ownership": "SHARED" },
        "ownerAttribute": "playedBy"
      },
      "action": { "type": "ADD_POINTS", "zone": { "name": "mesa", "ownership": "SHARED" }, "attribute": "points" }
    },
    {
      "event": "PLAY_CARD",
      "condition": { "type": "ZONE_CARD_COUNT_EQUALS", "zone": { "name": "mesa", "ownership": "SHARED" }, "count": 2 },
      "target": {
        "type": "WINNING_CARD_OWNER",
        "zone": { "name": "mesa", "ownership": "SHARED" },
        "rankAttribute": "strength",
        "groupAttribute": "suit",
        "preferredGroupZone": { "name": "triunfo", "ownership": "SHARED" },
        "ownerAttribute": "playedBy"
      },
      "action": { "type": "SET_CURRENT_PLAYER" }
    },
    {
      "event": "PLAY_CARD",
      "condition": { "type": "ZONE_CARD_COUNT_EQUALS", "zone": { "name": "mesa", "ownership": "SHARED" }, "count": 2 },
      "target": {
        "type": "WINNING_CARD_OWNER",
        "zone": { "name": "mesa", "ownership": "SHARED" },
        "rankAttribute": "strength",
        "groupAttribute": "suit",
        "preferredGroupZone": { "name": "triunfo", "ownership": "SHARED" },
        "ownerAttribute": "playedBy"
      },
      "action": {
        "type": "MOVE_ALL_CARDS",
        "from": { "name": "mesa", "ownership": "SHARED" },
        "to": { "name": "bazas", "ownership": "PER_PLAYER" }
      }
    }
  ]
}
```

## Ejemplo mínimo de reparto inicial

```json
{
  "zones": [
    { "name": "deck", "ownership": "SHARED", "shuffle": true },
    { "name": "hand", "ownership": "PER_PLAYER" }
  ],
  "cards": [
    { "id": "red-1", "zone": "deck", "count": 1, "attributes": { "color": "RED", "value": 1 } },
    { "id": "red-2", "zone": "deck", "count": 1, "attributes": { "color": "RED", "value": 2 } }
  ],
  "playerActions": ["DRAW_CARD"],
  "rules": [
    {
      "event": "GAME_STARTED",
      "condition": { "type": "AND", "conditions": [] },
      "target": { "type": "ALL_PLAYERS" },
      "action": {
        "type": "DRAW_CARDS",
        "from": { "name": "deck", "ownership": "SHARED" },
        "to": { "name": "hand", "ownership": "PER_PLAYER" },
        "count": 7
      }
    }
  ]
}
```

## Qué debe devolver tu respuesta

Responde **solo** con el JSON del `config`, sin explicación adicional y sin envolverlo en un
bloque de markdown (` ```json `), usando exactamente los nombres de capacidad y de campo de la
siguiente sección. Si algo que necesitas no existe en la lista de capacidades, dilo explícitamente
en vez de inventarte una capacidad nueva.

## Capacidades disponibles

Cada capacidad se referencia en el JSON con `"type": "NOMBRE"` dentro de `condition`/`target`/
`action` (las acciones se referencian igual, con `"type"`). El tipo de cada campo determina cómo
se escribe: `ZONE_REF` es un objeto `{ "name": ..., "ownership": ... }`, `CONDITION`/`ACTION` es
otro nodo anidado del mismo tipo, `CONDITION_LIST`/`ACTION_LIST` es un array de nodos, `ENUM` es
uno de los `valores posibles` listados, y `TEXT`/`INTEGER`/`BOOLEAN`/`SCALAR` son literales JSON.
