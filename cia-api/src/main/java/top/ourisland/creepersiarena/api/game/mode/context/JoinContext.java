package top.ourisland.creepersiarena.api.game.mode.context;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;

public record JoinContext(
        GameRuntime runtime,
        GameSession game,
        Player player,
        PlayerSession session
) {

}
