package top.ourisland.creepersiarena.game.mutation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.mutation.IMutationEffect;
import top.ourisland.creepersiarena.api.game.mutation.IMutationRegistry;
import top.ourisland.creepersiarena.api.game.mutation.MutationType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime-safe mutation effect registry. MutationService owns state and scheduling; this class owns effect providers.
 */
public final class MutationRegistry implements IMutationRegistry {

    private final Logger logger;
    private final IAbilityGate abilities;
    private final Map<MutationType, IMutationEffect> effects = new ConcurrentHashMap<>();
    private final Map<MutationType, String> effectOwners = new ConcurrentHashMap<>();

    public MutationRegistry(
            Logger logger,
            IAbilityGate abilities
    ) {
        this.logger = logger;
        this.abilities = abilities;
    }

    @Override
    public void registerMutation(String ownerId, IMutationEffect effect) {
        if (effect == null || effect.type() == null || effect.type().isNone()) return;
        effects.put(effect.type(), effect);
        effectOwners.put(effect.type(), ownerId == null || ownerId.isBlank() ? "unknown" : ownerId);
        reloadEffect(effect, mutationSection());
        logger.info("[Mutation] Registered effect {} by {}.", effect.type(), effectOwners.get(effect.type()));
    }

    private void reloadEffect(
            IMutationEffect effect,
            @Nullable ConfigurationSection section
    ) {
        try {
            effect.reload(section, logger);
        } catch (Throwable t) {
            logger.warn("[Mutation] Failed to reload {}: {}", effect.type(), t.getMessage(), t);
        }
    }

    private @Nullable ConfigurationSection mutationSection() {
        return abilities.config(CoreAbilities.MUTATION).settingsSection();
    }

    void reloadAll(@Nullable ConfigurationSection mutationSection) {
        for (var effect : effects.values()) {
            reloadEffect(effect, mutationSection);
        }
    }

    @Nullable IMutationEffect get(@Nullable MutationType type) {
        return type == null ? null : effects.get(type);
    }

    Collection<IMutationEffect> effects() {
        return List.copyOf(effects.values());
    }

    int size() {
        return effects.size();
    }

    void clearPlayer(@Nullable Player player) {
        for (var effect : effects.values()) {
            try {
                effect.clearPlayer(player);
            } catch (Throwable t) {
                logger.warn("[Mutation] Failed to clear player for {}: {}", effect.type(), t.getMessage(), t);
            }
        }
    }

}
