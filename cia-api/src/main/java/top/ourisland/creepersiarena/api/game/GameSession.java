package top.ourisland.creepersiarena.api.game;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class GameSession {

    @Getter private final GameModeId mode;
    private final Set<UUID> players = new HashSet<>();
    @Getter @Setter private ArenaInstance arena;

    public GameSession(
            GameModeId mode,
            ArenaInstance arena
    ) {
        this.mode = mode;
        this.arena = arena;
    }

    public @NonNull Set<UUID> players() {
        return Collections.unmodifiableSet(players);
    }

    public void addPlayer(UUID id) {
        players.add(id);
    }

    public void removePlayer(UUID id) {
        players.remove(id);
    }

}
