package top.ourisland.creepersiarena.game.mode;

import top.ourisland.creepersiarena.game.GameSession;

public interface IGameMode {
    GameModeType mode();

    ModeLogic createLogic(GameSession session, GameRuntime runtime);
}
