package top.ourisland.creepersiarena.game.lobby;

import org.bukkit.Location;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.core.region.Region2D;

public final class LobbyService {

    private final LobbyManager lobbyManager;

    public LobbyService(@lombok.NonNull LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    public Location lobbyAnchor(@NonNull String lobbyId) {
        return lobbyManager.anchorOrSpawn(lobbyId);
    }

    public @Nullable Region2D lobbyRegion(@NonNull String lobbyId) {
        return lobbyManager.region(lobbyId);
    }

    public @Nullable EntryZone entryZone(@NonNull String lobbyId) {
        return lobbyManager.entryZone(lobbyId);
    }
}
