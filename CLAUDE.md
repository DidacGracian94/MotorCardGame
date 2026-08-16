# MotorCardGame

Plataforma configurable y extensible para crear, guardar, reutilizar y ejecutar juegos de cartas
mediante un motor genérico + definiciones declarativas (JSON), no código específico por juego.

UNO es el primer caso de validación del motor. Brisca y Escoba son las siguientes pruebas de
generalización. Un juego inventado (ver hito M9) debe poder construirse solo con el editor, sin
tocar el motor, como prueba de que el sistema no está simplemente adaptado a UNO.

## Concepto fundamental — no romper nunca

Cuatro conceptos separados, sin excepciones:

- **GameDefinition**: configuración reutilizable de un juego (cartas, mazos, zonas, turnos, fases,
  reglas, condiciones de victoria). Responde a "¿cómo funciona este juego?". Inmutable una vez
  publicada una versión — modificarla crea una versión nueva.
- **GameInstance**: una partida concreta, creada a partir de una `GameDefinitionVersion` fija.
  Responde a "¿qué está pasando ahora mismo?". Tiene su propio estado mutable e independiente.
- **GameEngine**: interpreta una `GameDefinition` y ejecuta una `GameInstance`. No conoce juegos
  concretos.
- **GameEditor**: interfaz para crear/modificar `GameDefinition`. No contiene lógica de negocio.

Una `GameInstance` siempre está asociada a una **versión** concreta de la definición. Si el
propietario publica una versión nueva, las partidas en curso siguen usando la versión con la que
empezaron.

## Regla no negociable: configuración, no código

La configuración de un juego (JSON) nunca contiene código ejecutable (`"script"`, `"code"`, etc.).
Las reglas se expresan como `EVENT → CONDITION → TARGET → ACTION` usando capacidades que el motor
ya conoce (`DRAW_CARDS`, `CARD_TYPE_IS`, `NEXT_PLAYER`, ...). Si una mecánica nueva no puede
expresarse con las capacidades existentes, se añade una **capacidad genérica y reutilizable** al
motor — nunca un caso especial (`if game == "UNO"`, clases `UnoEngine`/`UnoCard`/...).

Antes de crear una capacidad nueva: ¿ya existe algo que resuelva esto? ¿se puede componer con lo
existente? ¿es realmente un concepto general o es específico de un juego? Preferir `CALCULATE_SCORE`
sobre `BRISCA_CALCULATE_SCORE`.

## Nota pendiente para cuando se diseñe el modelo de Zone en engine

Todavía no existe (llegará con el diseño de `engine`). Cuando se diseñe, pensar desde el principio
en dos ejes aunque el MVP (UNO) solo necesite el caso simple — para no rediseñar con partidas ya
persistidas usando un modelo viejo:

- **Ownership de la zona**: `SHARED` (un mazo/pila compartido, como en UNO/Brisca/Escoba) vs
  `PER_PLAYER` (cada jugador tiene su propia instancia — necesario para juegos de combate estilo
  Magic/Clash Royale, donde cada jugador lleva su propio mazo).
- **Topología de la zona**: pila lineal (orden, tope, fondo — lo único que cubre el roadmap actual
  con UNO/Brisca/Escoba) vs espacial/grid (posiciones, adyacencia — necesario para juegos de cartas
  con tablero).

`GameDefinitionVersion.config` es JSONB y opaco para `app`, así que esta decisión no afecta a las
tablas/entidades de `app` (`game_definitions`, `game_definition_versions`) — vive enteramente dentro
del modelo de `engine`. Es la misma filosofía de "capacidad genérica reutilizable" de la sección
anterior, aplicada al modelo de zonas en vez de a las reglas.

## Arquitectura

```
pom.xml (parent multi-módulo)
├── backend/
│   ├── pom.xml (backend parent)
│   ├── engine/
│   │   ├── pom.xml
│   │   └── src/
│   │       Java puro, SIN Spring/JPA. Modelo de dominio, registries de capacidades
│   │       (ActionRegistry, ConditionRegistry, TargetRegistry), RuleEngine, validación
│   │       de GameDefinition. Debe poder probarse con JUnit sin levantar contexto Spring.
│   │
│   └── app/
│       ├── pom.xml (depende de engine)
│       └── src/
│           Spring Boot. Persistencia (JPA + Flyway), API REST, WebSocket/STOMP,
│           seguridad (JWT propio). Orquesta el engine, no reimplementa reglas.
│
└── frontend/
    ├── package.json
    └── src/
        React + TypeScript + Vite. Renderiza el estado que manda el motor y envía
        PlayerAction. Sin lógica de reglas de ningún juego en el cliente.
```

**Regla de dependencia**: `frontend` → habla por HTTP/WS con `app` → depende de `engine` → no depende
de nada del resto. Nunca al revés.

**Maven multi-módulo**:
- POM parent en raíz: gestiona versiones, propiedades comunes, perfiles (dev/test/prod)
- Backend parent (`backend/pom.xml`): Spring Boot version, dependencias comunes (JUnit, Spring)
- Cada módulo (`engine`, `app`) tiene su propio POM que hereda del parent de backend
- Frontend está **fuera** de Maven (npm/yarn independiente)

## Stack

- Backend: Java 25, Spring Boot 3 (Web, Security, Data JPA, WebSocket, Validation), Maven
  multi-módulo (parent + `engine` + `app`)
- DB: PostgreSQL 16 + Flyway. Identidad/versión/visibilidad en columnas normalizadas; config de
  juego y estado de partida en JSONB (`game_definition_versions.config`,
  `game_instance_state.state`); `game_events` append-only para historial/replay.
- Frontend: React 18 + TypeScript + Vite, TanStack Query (estado servidor), Zustand (estado de
  partida en vivo), React Hook Form + Zod (editor), Tailwind + Radix (headless, sin design system
  pesado)
- Tiempo real: WebSocket STOMP — `/topic/games/{id}/public` (estado público) +
  `/user/queue/games/{id}/private` (mano del jugador y demás info privada). La visibilidad de
  información es una propiedad del transporte, no una decisión de UI.
- Auth: Spring Security + JWT propio (access corto + refresh rotado), BCrypt.
- Testing: JUnit 5 (engine, unit + integración de partidas completas), Testcontainers/Postgres
  (persistencia — no H2, porque JSONB es central), MockMvc (API), Vitest + Testing Library
  (frontend).

## Editor de reglas — decisión tomada

Formulario `WHEN / IF / THEN` encadenado que lee su metadata desde `/api/capabilities`
(ActionRegistry/ConditionRegistry/TargetRegistry expuestos), no un canvas de nodos tipo React Flow.
Más simple de construir y validar para el MVP; el canvas de nodos queda como evolución posterior si
hace falta.

## Desarrollo con Maven

Comandos comunes desde la raíz del proyecto:

```bash
# Compilar todo (parent + backend + frontend)
mvn clean install

# Compilar solo backend (parent + engine + app)
mvn clean install -DskipFrontend

# Ejecutar tests del engine (sin Spring)
mvn test -pl backend/engine

# Ejecutar tests de integración del app (con Testcontainers)
mvn verify -pl backend/app

# Ejecutar específicamente la app (Spring Boot)
mvn spring-boot:run -pl backend/app

# Empaquetar JAR de producción
mvn clean package -DskipTests

# Limpiar target/ de todos los módulos
mvn clean
```

El archivo `pom.xml` raíz maneja versionado semántico. Cada cambio que afecte a la API del `engine`
puede requerir bump de versión (patch/minor/major según compatibilidad).

## Checklist antes de dar por buena cualquier PR/cambio

- ¿Hay alguna clase `Uno*`/`Brisca*`/`Escoba*` en `engine`? No debería.
- ¿Se ha editado una versión ya publicada en vez de crear una nueva? No se debe.
- ¿El frontend cambia estado sin pasar por una acción validada por el motor? No se debe.
- ¿Hay `"script"`/`"code"` en algún JSON de configuración? No se debe.
- ¿Puede viajar la mano de un jugador por el canal público de WS? No se debe.
- ¿Se ha añadido o cambiado un endpoint? Debe reflejarse también en la colección de Postman
  (`backend/app/src/test/collection/MotorCardGame.postman_collection.json`) — si no, queda
  desactualizada y deja de servir para probar la API manualmente.

## Estado actual

Repositorio vacío, sin scaffold todavío. Próximo hito: **M0 — andamiaje** (Maven multi-módulo,
Spring Boot arrancando, Flyway baseline, React+Vite arrancando, docker-compose con Postgres).

Hoja de ruta completa (M0–M11, con criterios de salida por hito): ver el plan de arquitectura
publicado — pedir a Claude que lo recupere si hace falta releerlo (fue publicado como artifact en
una sesión anterior de Claude Code).

## Referencias útiles

- **Java/Maven**: Java 25, Maven 3.9+, Spring Boot 3.x BOM
- **Base de datos**: PostgreSQL 16, driver JDBC, Flyway para migraciones
- **Testing**: JUnit 5, Testcontainers (PostgreSQL), MockMvc, Vitest + Testing Library
- **Documentación de capacidades**: cada módulo (engine, app) debe incluir README con ejemplos de
  uso de registries (ActionRegistry, ConditionRegistry, TargetRegistry)
- **Seguridad**: Revisar OWASP Top 10; JWT debe usar RS256 (asymmetric), no HS256
