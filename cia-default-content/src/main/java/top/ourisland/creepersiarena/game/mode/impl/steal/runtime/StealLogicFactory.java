package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.ModeLogic;

/**
 * Builds the collaborating runtime objects for one steal-mode game session.
 */
public final class StealLogicFactory {

    private StealLogicFactory() {
    }

    public static ModeLogic create(GameSession session, GameRuntime runtime) {
        var state = new StealState();
        return new ModeLogic(
                new StealRules(runtime, session, state),
                new StealTimeline(runtime, session, state),
                new StealPlayerFlow(runtime, state)
        );
    }

}
