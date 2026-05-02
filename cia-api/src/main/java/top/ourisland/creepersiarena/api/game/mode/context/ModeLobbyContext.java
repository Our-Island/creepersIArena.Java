package top.ourisland.creepersiarena.api.game.mode.context;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;

/**
 * Context passed to mode-owned lobby UI hooks.
 */
public record ModeLobbyContext(
        GameRuntime runtime,
        Player player,
        PlayerSession session
) {

}
