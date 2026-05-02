package top.ourisland.creepersiarena.game.mode.impl.steal;

import top.ourisland.creepersiarena.core.component.annotation.CiaModeDef;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.mode.IGameMode;
import top.ourisland.creepersiarena.game.mode.ModeLogic;

@CiaModeDef(id = "steal")
public final class StealMode implements IGameMode {

    @Override
    public ModeLogic createLogic(GameSession session, GameRuntime runtime) {
        var state = new StealState();
        return new ModeLogic(
                new StealRules(runtime, session, state),
                new StealTimeline(runtime, session, state)
        );
    }

}
