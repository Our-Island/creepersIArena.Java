package top.ourisland.creepersiarena.game.mode.context;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.player.PlayerSession;

public record JoinContext(
        GameRuntime runtime,
        GameSession game,
        Player player,
        PlayerSession session
) {
}
