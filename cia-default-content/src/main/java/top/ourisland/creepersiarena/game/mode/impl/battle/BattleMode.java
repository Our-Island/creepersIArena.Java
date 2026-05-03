package top.ourisland.creepersiarena.game.mode.impl.battle;

import top.ourisland.creepersiarena.api.annotation.CiaModeDef;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.game.mode.ModeLogic;
import top.ourisland.creepersiarena.game.mode.impl.battle.config.BattleModeConfig;

@CiaModeDef(id = "battle")
public final class BattleMode implements IGameMode {

    @Override
    public ModeLogic createLogic(GameSession session, GameRuntime runtime) {
        BattleModeConfig config = BattleModeConfig.from(runtime.cfg());
        BattleState state = new BattleState(runtime, session, config);
        BattleTeamBalancer teams = new BattleTeamBalancer(config, runtime.sessionStore());
        BattleBossBars bossBars = new BattleBossBars();

        return new ModeLogic(
                new BattleRules(state),
                new BattleTimeline(state, bossBars),
                new BattlePlayerFlow(runtime, state, teams)
        );
    }

}
