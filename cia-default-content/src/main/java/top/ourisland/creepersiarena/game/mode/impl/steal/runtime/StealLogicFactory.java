package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.ModeLogic;
import top.ourisland.creepersiarena.game.mode.impl.steal.config.StealArenaConfig;
import top.ourisland.creepersiarena.game.mode.impl.steal.config.StealModeConfig;

/**
 * Builds the collaborating runtime objects for one steal-mode game session.
 */
public final class StealLogicFactory {

    private StealLogicFactory() {
    }

    public static ModeLogic create(GameSession session, GameRuntime runtime) {
        var modeConfig = StealModeConfig.from(runtime.cfg());
        var arenaConfig = StealArenaConfig.from(session.arena());
        var state = new StealState(modeConfig, arenaConfig);
        var lobbyUi = new StealLobbyUi(runtime, state);
        return new ModeLogic(
                new StealRules(runtime, session, state),
                new StealTimeline(runtime, session, state, lobbyUi),
                new StealPlayerFlow(runtime, state, lobbyUi)
        );
    }

}
