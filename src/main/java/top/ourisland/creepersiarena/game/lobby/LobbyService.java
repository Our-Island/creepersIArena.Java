package top.ourisland.creepersiarena.game.lobby;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.game.region.Region2D;

import java.util.Objects;

public final class LobbyService {

    private final LobbyManager lobbyManager;

    public LobbyService(@NotNull LobbyManager lobbyManager) {
        this.lobbyManager = Objects.requireNonNull(lobbyManager, "lobbyManager");
    }

    public Location hubAnchor() {
        return lobbyAnchor("hub");
    }

    public Location lobbyAnchor(@NotNull String lobbyId) {
        return lobbyManager.anchorOrSpawn(lobbyId);
    }

    public Location deathAnchor() {
        return lobbyAnchor("death");
    }

    public @NonNull Region2D lobbyRegion(String lobbyId) {
        return lobbyManager.region(lobbyId);
    }
}
