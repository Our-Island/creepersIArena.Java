package top.ourisland.creepersiarena.game.mode.impl.steal;

import top.ourisland.creepersiarena.api.annotation.CiaModeDef;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.game.mode.ModeLogic;
import top.ourisland.creepersiarena.game.mode.impl.steal.runtime.StealLogicFactory;

@CiaModeDef(id = "steal")
public final class StealMode implements IGameMode {

    @Override
    public ModeLogic createLogic(GameSession session, GameRuntime runtime) {
        return StealLogicFactory.create(session, runtime);
    }

}
