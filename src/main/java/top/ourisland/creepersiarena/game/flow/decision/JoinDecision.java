package top.ourisland.creepersiarena.game.flow.decision;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * 模式只做决策，不做动作；GameFlow 负责执行。
 */
public sealed interface JoinDecision {

    record ToHub() implements JoinDecision {
    }

    record ToSpectate(@Nullable Location where) implements JoinDecision {
    }

    /**
     * 直接进战场（一般不推荐 join 就进，但留给未来扩展）
     */
    record ToBattle() implements JoinDecision {
    }
}
