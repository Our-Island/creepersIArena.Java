package top.ourisland.creepersiarena.game.lobby;

import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ourisland.creepersiarena.game.region.Region2D;

public final class LobbyService {

    private final LobbyManager lobbyManager;

    public LobbyService(@NonNull LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    public Location lobbyAnchor(@NotNull String lobbyId) {
        return lobbyManager.anchorOrSpawn(lobbyId);
    }

    public @Nullable Region2D lobbyRegion(@NotNull String lobbyId) {
        return lobbyManager.region(lobbyId);
    }

    public @Nullable EntryZone entryZone(@NotNull String lobbyId) {
        return lobbyManager.entryZone(lobbyId);
    }
}
