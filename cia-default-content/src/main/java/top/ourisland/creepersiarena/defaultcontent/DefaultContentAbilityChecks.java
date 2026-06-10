package top.ourisland.creepersiarena.defaultcontent;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;

/**
 * Small default-content helper for runtime ability checks.
 */
public final class DefaultContentAbilityChecks {

    private DefaultContentAbilityChecks() {
    }

    public static boolean enabled(
            @Nullable GameRuntime runtime,
            @Nullable GameSession session,
            AbilityId abilityId,
            @Nullable String phase,
            @Nullable String reason
    ) {
        return enabled(runtime, session, null, abilityId, phase, reason);
    }

    public static boolean enabled(
            @Nullable GameRuntime runtime,
            @Nullable GameSession session,
            @Nullable Player player,
            AbilityId abilityId,
            @Nullable String phase,
            @Nullable String reason
    ) {
        if (runtime == null || abilityId == null) return false;

        var gate = runtime.getService(IAbilityGate.class);
        if (gate == null) return false;

        var builder = gate.contextBuilder(player, reason)
                .runtime(runtime)
                .game(session);
        if (phase != null && !phase.isBlank()) builder.phase(phase);
        return gate.isEnabled(abilityId, builder.build());
    }

}
