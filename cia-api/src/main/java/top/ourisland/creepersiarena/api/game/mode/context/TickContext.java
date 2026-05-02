package top.ourisland.creepersiarena.api.game.mode.context;

import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;

public record TickContext(
        GameRuntime runtime,
        GameSession session
) {

}
