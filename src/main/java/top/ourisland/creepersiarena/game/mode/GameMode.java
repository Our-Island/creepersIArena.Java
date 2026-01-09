package top.ourisland.creepersiarena.game.mode;

import top.ourisland.creepersiarena.game.GameSession;

public interface GameMode {
    GameModeType mode();

    ModeLogic createLogic(GameSession session, GameRuntime runtime);
}
