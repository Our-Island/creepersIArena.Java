package top.ourisland.creepersiarena.game.player;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerSessionStore {
    private final Map<UUID, PlayerSession> sessions = new HashMap<>();

    public PlayerSession getOrCreate(Player p) {
        return sessions.computeIfAbsent(p.getUniqueId(), id -> new PlayerSession(p));
    }

    public PlayerSession get(Player p) {
        return sessions.get(p.getUniqueId());
    }

    public void remove(Player p) {
        sessions.remove(p.getUniqueId());
    }
}
