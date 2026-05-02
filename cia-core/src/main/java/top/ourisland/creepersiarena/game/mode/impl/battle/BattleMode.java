package top.ourisland.creepersiarena.game.mode.impl.battle;

import top.ourisland.creepersiarena.core.component.annotation.CiaModeDef;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.mode.IGameMode;
import top.ourisland.creepersiarena.game.mode.ModeLogic;

@CiaModeDef(id = "battle")
public final class BattleMode implements IGameMode {

    @Override
    public ModeLogic createLogic(GameSession session, GameRuntime runtime) {
        return new ModeLogic(
                new BattleRules(runtime, session),
                new BattleTimeline(runtime, session)
        );
    }

}
