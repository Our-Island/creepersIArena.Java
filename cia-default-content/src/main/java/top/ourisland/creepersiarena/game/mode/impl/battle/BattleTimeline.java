package top.ourisland.creepersiarena.game.mode.impl.battle;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.flow.action.GameAction;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModeTimeline;
import top.ourisland.creepersiarena.api.game.mode.context.TickContext;
import top.ourisland.creepersiarena.game.mode.impl.battle.config.BattleModeConfig;

import java.util.List;

public final class BattleTimeline implements IModeTimeline {

    private final GameRuntime runtime;
    private final GameSession session;

    private int remaining;

    public BattleTimeline(GameRuntime runtime, GameSession session) {
        this.runtime = runtime;
        this.session = session;
        this.remaining = BattleModeConfig.from(runtime.cfg()).singleGameTimeSeconds();
    }

    @Override
    public GameModeType type() {
        return GameModeType.of("battle");
    }

    @Override
    public List<GameAction> tick(TickContext ctx) {
        if (remaining <= 0) return List.of();

        remaining--;
        if (remaining > 0) return List.of();

        // TODO: 结算/换图：先最小实现 -> 全部回 hub
        return List.of(
                new GameAction.Broadcast(Component.text("BATTLE：本局结束！", NamedTextColor.GOLD)),
                new GameAction.EndGameAndBackToHub("BATTLE_TIMEOUT")
        );
    }

}
