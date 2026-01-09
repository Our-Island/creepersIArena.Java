package top.ourisland.creepersiarena.game.mode.impl.battle;

import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.mode.GameMode;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.mode.ModeLogic;

public final class BattleMode implements GameMode {
    @Override
    public GameModeType mode() {
        return GameModeType.BATTLE;
    }

    @Override
    public ModeLogic createLogic(GameSession session, GameRuntime runtime) {
        return new ModeLogic(
                new BattleRules(runtime, session),
                new BattleTimeline(runtime, session)
        );
    }
}
