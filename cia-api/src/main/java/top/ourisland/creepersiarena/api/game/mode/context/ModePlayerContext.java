package top.ourisland.creepersiarena.api.game.mode.context;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;

/**
 * Context passed to mode-owned player flow hooks.
 * <p>
 * Core owns the high-level player state machine, but the active mode owns gameplay-specific details such as the spawn
 * point, loadout, mode HUD/action-bar messages, and mode-local session data.
 */
public record ModePlayerContext(
        GameRuntime runtime,
        GameSession game,
        Player player,
        PlayerSession session,
        Location fallback
) {

}
