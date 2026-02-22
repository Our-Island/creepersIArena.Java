package top.ourisland.creepersiarena.game.protection;

import org.bukkit.Location;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.core.region.Region2D;

/**
 * 纯判断层：给监听器/状态/规则提供“是否在范围内”的查询。 不依赖 Bukkit Event。
 */
public final class ProtectionService {

    private final ArenaManager arenaManager;
    private final LobbyService lobbyService;

    public ProtectionService(
            @lombok.NonNull ArenaManager arenaManager,
            @lombok.NonNull LobbyService lobbyService
    ) {
        this.arenaManager = arenaManager;
        this.lobbyService = lobbyService;
    }

    public boolean isInLobby(String lobbyId, Location loc) {
        Region2D r = lobbyService.lobbyRegion(lobbyId);
        return r != null && r.contains(loc);
    }

    public boolean isInArena(String arenaId, Location loc) {
        ArenaInstance a = arenaManager.getArena(arenaId);
        return a != null && a.region().contains(loc);
    }

    public @Nullable ArenaInstance findArena(Location loc) {
        for (ArenaInstance a : arenaManager.arenas()) {
            if (a.region().contains(loc)) return a;
        }
        return null;
    }
}
