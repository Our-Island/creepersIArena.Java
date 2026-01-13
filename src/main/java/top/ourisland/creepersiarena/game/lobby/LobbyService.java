package top.ourisland.creepersiarena.game.lobby;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ourisland.creepersiarena.game.region.Region2D;

import java.util.Objects;

public final class LobbyService {

    private final LobbyManager lobbyManager;

    public LobbyService(@NotNull LobbyManager lobbyManager) {
        this.lobbyManager = Objects.requireNonNull(lobbyManager, "lobbyManager");
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
