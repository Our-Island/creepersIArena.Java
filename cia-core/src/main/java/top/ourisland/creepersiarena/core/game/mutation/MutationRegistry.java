package top.ourisland.creepersiarena.core.game.mutation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.mutation.IMutationEffect;
import top.ourisland.creepersiarena.api.game.mutation.IMutationRegistry;
import top.ourisland.creepersiarena.api.game.mutation.MutationId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.identity.OwnedRegistry;

import java.util.Collection;

public final class MutationRegistry implements IMutationRegistry {

    private final Logger logger;
    private final IAbilityGate abilities;
    private final OwnedRegistry<MutationId, IMutationEffect> effects;

    public MutationRegistry(
            Logger logger,
            IAbilityGate abilities,
            NamespaceRegistry namespaces
    ) {
        this.logger = logger;
        this.abilities = abilities;
        this.effects = new OwnedRegistry<>(namespaces);
    }

    @Override
    public void registerMutation(RegistrationOwner owner, IMutationEffect effect) {
        if (effect.type().isNone()) {
            throw new IllegalArgumentException("core:none is reserved and cannot be registered as a mutation effect");
        }
        effects.register(owner, effect.type(), effect);
        reloadEffect(effect, mutationSection());
        logger.info("[Mutation] Registered effect {} by {}.", effect.type(), owner.extensionId());
    }

    private void reloadEffect(
            IMutationEffect effect,
            @Nullable ConfigurationSection mutationSettings
    ) {
        try {
            ConfigurationSection effectSection = null;
            if (mutationSettings != null) {
                var effectsSection = mutationSettings.getConfigurationSection("effects");
                if (effectsSection != null) effectSection = effectsSection.getConfigurationSection(effect.configKey());
            }
            effect.reload(effectSection, logger);
        } catch (Throwable throwable) {
            logger.warn("[Mutation] Failed to reload {}: {}", effect.type(), throwable.getMessage(), throwable);
        }
    }

    private @Nullable ConfigurationSection mutationSection() {
        return abilities.config(CoreAbilities.MUTATION).settingsSection();
    }

    public void clearOwner(RegistrationOwner owner) {
        effects.clearOwner(owner);
    }

    void reloadAll(@Nullable ConfigurationSection mutationSection) {
        for (var effect : effects.values()) reloadEffect(effect, mutationSection);
    }

    @Nullable IMutationEffect get(@Nullable MutationId type) {
        if (type == null) return null;
        var registered = effects.get(type);
        return registered == null ? null : registered.value();
    }

    Collection<IMutationEffect> effects() {
        return effects.values();
    }

    int size() {
        return effects.entries().size();
    }

    void clearPlayer(@Nullable Player player) {
        for (var effect : effects.values()) {
            try {
                effect.clearPlayer(player);
            } catch (Throwable throwable) {
                logger.warn("[Mutation] Failed to clear player for {}: {}", effect.type(), throwable.getMessage(), throwable);
            }
        }
    }

}
