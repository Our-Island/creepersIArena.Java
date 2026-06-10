package top.ourisland.creepersiarena.core.ability;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.AbilityContext;
import top.ourisland.creepersiarena.api.game.mode.IModeAbilityPolicy;
import top.ourisland.creepersiarena.core.game.GameManager;

import java.util.function.Supplier;

public final class AbilityContextFactory {

    private final Logger logger;
    private final Supplier<GameManager> gameManager;

    public AbilityContextFactory(
            Logger logger,
            Supplier<GameManager> gameManager
    ) {
        this.logger = logger;
        this.gameManager = gameManager;
    }

    public @NonNull AbilityContext forPlayer(
            @Nullable Player player,
            @Nullable String reason
    ) {
        return builder(player, reason).build();
    }

    public AbilityContext.Builder builder(
            @Nullable Player player,
            @Nullable String reason
    ) {
        var gm = gameManager == null ? null : gameManager.get();
        var runtime = gm == null ? null : gm.runtime();
        var active = gm == null ? null : gm.active();

        var builder = AbilityContext.builder()
                .runtime(runtime)
                .game(active)
                .player(player)
                .reason(reason);

        if (gm != null) {
            contribute(gm.rules(), builder);
            contribute(gm.timeline(), builder);
            contribute(gm.playerFlow(), builder);
        }

        return builder;
    }

    private void contribute(Object candidate, AbilityContext.Builder builder) {
        if (!(candidate instanceof IModeAbilityPolicy policy)) return;
        try {
            policy.contributeAbilityContext(builder);
        } catch (Throwable t) {
            if (logger != null) logger.warn("[Ability] mode context contribution failed: {}", t.getMessage(), t);
        }
    }

    public @NonNull AbilityContext forGame(@Nullable String reason) {
        return builder(null, reason).build();
    }

}
