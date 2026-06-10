package top.ourisland.creepersiarena.core.ability;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.ability.AbilityContext;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.IAbilityConfigView;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;

/**
 * Default runtime ability gate backed by the ability service and the internal context factory.
 */
public final class AbilityGate implements IAbilityGate {

    private final AbilityService abilities;
    private final AbilityContextFactory contexts;

    public AbilityGate(
            @lombok.NonNull AbilityService abilities,
            @lombok.NonNull AbilityContextFactory contexts
    ) {
        this.abilities = abilities;
        this.contexts = contexts;
    }

    @Override
    public AbilityContext.Builder contextBuilder(
            @Nullable Player player,
            @Nullable String reason
    ) {
        return contexts.builder(player, reason);
    }

    @Override
    public IAbilityConfigView config(AbilityId abilityId) {
        return abilities.config(abilityId);
    }

    @Override
    public boolean isEnabled(AbilityId abilityId, AbilityContext context) {
        return abilities.isEnabled(abilityId, context);
    }

    @Override
    public @NonNull AbilityContext contextForPlayer(
            @Nullable Player player,
            @Nullable String reason
    ) {
        return contexts.forPlayer(player, reason);
    }

    @Override
    public @NonNull AbilityContext contextForGame(@Nullable String reason) {
        return contexts.forGame(reason);
    }

}
