package top.ourisland.creepersiarena.api.game.flow.decision;

import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

public sealed interface RespawnDecision {

    /**
     * Move to the generic respawn lobby and return to the active game after the countdown.
     */
    record RespawnLobbyCountdown(
            int seconds
    ) implements RespawnDecision {

    }

    /**
     * Move to spectator mode, optionally at a supplied location.
     */
    record Spectate(
            @Nullable Location where
    ) implements RespawnDecision {

    }

    /**
     * Directly return to the hub.
     */
    record Hub() implements RespawnDecision {

    }

}
