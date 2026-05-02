package top.ourisland.creepersiarena.api.game.mode.context;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;

public record JoinContext(
        GameRuntime runtime,
        GameSession game,
        Player player,
        PlayerSession session,
        JoinSource source
) {

    public JoinContext(
            GameRuntime runtime,
            GameSession game,
            Player player,
            PlayerSession session
    ) {
        this(runtime, game, player, session, JoinSource.SERVER_JOIN);
    }

    public boolean fromHubRequest() {
        return source == JoinSource.HUB_REQUEST;
    }

}
