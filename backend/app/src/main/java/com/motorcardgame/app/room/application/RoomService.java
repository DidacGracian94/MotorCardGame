package com.motorcardgame.app.room.application;

import com.motorcardgame.app.auth.application.JwtService;
import com.motorcardgame.app.auth.domain.Role;
import com.motorcardgame.app.gamedefinition.instance.application.GameInstanceService;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.version.application.GameDefinitionVersionService;
import com.motorcardgame.app.room.domain.GameRoom;
import com.motorcardgame.app.room.domain.RoomPlayer;
import com.motorcardgame.app.room.domain.RoomRepository;
import com.motorcardgame.app.room.domain.RoomStatus;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    /** Sin 0/O ni 1/I/L: caracteres que se confunden fácilmente al leer un código en voz alta. */
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private final RoomRepository repository;
    private final GameDefinitionVersionService gameDefinitionVersionService;
    private final GameInstanceService gameInstanceService;
    private final JwtService jwtService;
    private final SecureRandom random = new SecureRandom();

    public RoomService(
            RoomRepository repository,
            GameDefinitionVersionService gameDefinitionVersionService,
            GameInstanceService gameInstanceService,
            JwtService jwtService) {
        this.repository = repository;
        this.gameDefinitionVersionService = gameDefinitionVersionService;
        this.gameInstanceService = gameInstanceService;
        this.jwtService = jwtService;
    }

    public GameRoom create(UUID gameDefinitionId, int versionNumber, UUID hostUserId, Role hostRole) {
        gameDefinitionVersionService.getByVersionNumber(gameDefinitionId, versionNumber, hostUserId, hostRole);
        String code = generateUniqueCode();
        GameRoom room = GameRoom.create(code, gameDefinitionId, versionNumber, hostUserId);
        repository.save(room);
        return room;
    }

    public GameRoom getByCode(String code) {
        return repository.findByCode(normalize(code)).orElseThrow(() -> new RoomNotFoundException(code));
    }

    /**
     * Un jugador se une solo con un nombre, sin cuenta. Para que pueda usar el resto de la API
     * (consultar/actuar sobre la partida una vez arranque, conectar al WebSocket) sin tocar ni
     * relajar la autenticación existente, se le emite un access token JWT normal para un usuario
     * "invitado" que nunca se persiste en {@code users} — el token es autocontenido (RS256, sin
     * consulta a base de datos al validarlo, ver JwtService) así que no hace falta que exista fila
     * alguna. No lleva refresh token: la sesión de invitado dura lo que dure el access token.
     */
    public RoomJoinResult join(String code, String displayName) {
        GameRoom room = getByCode(code);
        RoomPlayer player = room.join(displayName);
        String accessToken = jwtService.issueAccessToken(UUID.randomUUID(), Role.USER);
        return new RoomJoinResult(player, accessToken, room);
    }

    public GameRoom start(String code, UUID hostUserId, Role hostRole) {
        GameRoom room = getByCode(code);
        if (!room.hostUserId().equals(hostUserId) && hostRole != Role.ADMIN) {
            throw new RoomAccessDeniedException(code);
        }
        if (room.status() != RoomStatus.OPEN) {
            throw new RoomNotOpenException(code);
        }
        List<Player> players = room.players().stream()
                .map(p -> new Player(new PlayerId(p.id()), p.displayName()))
                .toList();
        if (players.isEmpty()) {
            throw new RoomEmptyException(code);
        }
        // La comprobación de arriba evita crear una GameInstance huérfana en el caso normal; la
        // que hace room.start() por dentro (sincronizada sobre la propia sala) es la que de verdad
        // decide en la carrera improbable de dos "empezar" concurrentes — si pierde, se acepta la
        // GameInstance ya creada como desperdicio menor en vez de intentar deshacerla.
        GameInstance instance = gameInstanceService.create(
                room.gameDefinitionId(), room.versionNumber(), players, hostUserId, hostRole);
        room.start(instance.id());
        return room;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = randomCode();
        } while (repository.existsByCode(code));
        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

    private static String normalize(String code) {
        return code.toUpperCase();
    }

    public record RoomJoinResult(RoomPlayer player, String accessToken, GameRoom room) {
    }
}
