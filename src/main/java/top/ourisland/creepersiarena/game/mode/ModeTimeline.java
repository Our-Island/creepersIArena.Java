package top.ourisland.creepersiarena.game.mode;

import top.ourisland.creepersiarena.game.flow.action.GameAction;
import top.ourisland.creepersiarena.game.mode.context.TickContext;

import java.util.List;

public interface ModeTimeline {
    GameModeType type();

    List<GameAction> tick(TickContext ctx);
}
