package top.ourisland.creepersiarena.game.mode.context;

import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.mode.GameRuntime;

public record TickContext(GameRuntime runtime, GameSession session) {
}
