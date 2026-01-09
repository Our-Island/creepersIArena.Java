package top.ourisland.creepersiarena.game.flow.decision;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public sealed interface RespawnDecision {

    /**
     * BATTLE：进 death lobby 并启动倒计时，倒计时结束后回战场。
     */
    record DeathLobbyCountdown(int seconds) implements RespawnDecision {
    }

    /**
     * STEAL：直接旁观（可选指定旁观点）
     */
    record Spectate(@Nullable Location where) implements RespawnDecision {
    }

    /**
     * 直接回大厅
     */
    record Hub() implements RespawnDecision {
    }
}
