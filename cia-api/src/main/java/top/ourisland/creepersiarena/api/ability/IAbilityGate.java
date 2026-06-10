package top.ourisland.creepersiarena.api.ability;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Runtime-facing read-only ability helper.
 * <p>
 * Runtime systems should depend on this gate instead of carrying both a registry and a project-specific context
 * factory. Registration belongs to {@link IAbilityRegistry}; admin overrides and diagnostics belong to
 * {@link IAbilityAdmin}.
 */
public interface IAbilityGate {

    AbilityContext.Builder contextBuilder(
            @Nullable Player player,
            @Nullable String reason
    );

    IAbilityConfigView config(AbilityId abilityId);

    default boolean isEnabled(
            AbilityId abilityId,
            @Nullable Player player,
            @Nullable String reason
    ) {
        return isEnabled(abilityId, contextForPlayer(player, reason));
    }

    boolean isEnabled(AbilityId abilityId, AbilityContext context);

    @NonNull AbilityContext contextForPlayer(
            @Nullable Player player,
            @Nullable String reason
    );

    default boolean isEnabledForGame(
            AbilityId abilityId,
            @Nullable String reason
    ) {
        return isEnabled(abilityId, contextForGame(reason));
    }

    @NonNull AbilityContext contextForGame(@Nullable String reason);


}
