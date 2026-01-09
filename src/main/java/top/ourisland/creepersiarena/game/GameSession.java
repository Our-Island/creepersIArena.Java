package top.ourisland.creepersiarena.game;

import top.ourisland.creepersiarena.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.game.mode.GameModeType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class GameSession {
    private final GameModeType mode;
    private final Set<UUID> players = new HashSet<>();
    private ArenaInstance arena;

    public GameSession(GameModeType mode, ArenaInstance arena) {
        this.mode = mode;
        this.arena = arena;
    }

    public GameModeType mode() {
        return mode;
    }

    public ArenaInstance arena() {
        return arena;
    }

    public void arena(ArenaInstance arena) {
        this.arena = arena;
    }

    public Set<UUID> players() {
        return Collections.unmodifiableSet(players);
    }

    public void addPlayer(UUID id) {
        players.add(id);
    }

    public void removePlayer(UUID id) {
        players.remove(id);
    }
}
