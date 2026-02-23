package top.ourisland.creepersiarena.game.mode.impl.steal;

import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.mode.IGameMode;
import top.ourisland.creepersiarena.game.mode.ModeLogic;

public final class StealMode implements IGameMode {
    @Override
    public GameModeType mode() {
        return GameModeType.STEAL;
    }

    @Override
    public ModeLogic createLogic(GameSession session, GameRuntime runtime) {
        var state = new top.ourisland.creepersiarena.game.mode.impl.steal.StealState();
        return new ModeLogic(
                new StealRules(runtime, session, state),
                new StealTimeline(runtime, session, state)
        );
    }
}
