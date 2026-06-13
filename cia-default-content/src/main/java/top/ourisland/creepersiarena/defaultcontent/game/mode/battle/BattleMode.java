package top.ourisland.creepersiarena.defaultcontent.game.mode.battle;

import top.ourisland.creepersiarena.api.annotation.CiaModeDef;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.game.mode.ModeLogic;
import top.ourisland.creepersiarena.defaultcontent.game.mode.battle.config.BattleModeConfig;

@CiaModeDef(id = "cia:battle")
public final class BattleMode implements IGameMode {

    @Override
    public ModeLogic createLogic(GameSession session, GameRuntime runtime) {
        var config = BattleModeConfig.from(runtime.cfg());
        var state = new BattleState(runtime, session, config);
        var teams = new BattleTeamBalancer(config, runtime.sessionStore());
        var bossBars = new BattleBossBars();

        return new ModeLogic(
                new BattleRules(state),
                new BattleTimeline(state, bossBars),
                new BattlePlayerFlow(runtime, state, teams)
        );
    }

}
