package top.ourisland.creepersiarena.game.death;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;

public final class DeathCleanupService {

    private static final double DEFAULT_MAX_HEALTH = 20.0D;
    private static final double DEFAULT_MAX_ABSORPTION = 0.0D;
    private static final double DEFAULT_KNOCKBACK_RESISTANCE = 0.0D;

    private final Logger log;
    private final PlayerSessionStore store;
    private final DamageAttributionStore attributionStore;
    private final DeathResolutionRegistry registry;
    private final IAbilityGate abilities;

    public DeathCleanupService(
            @lombok.NonNull Logger log,
            @lombok.NonNull PlayerSessionStore store,
            @lombok.NonNull DamageAttributionStore attributionStore,
            @lombok.NonNull DeathResolutionRegistry registry,
            @lombok.NonNull IAbilityGate abilities
    ) {
        this.log = log;
        this.store = store;
        this.attributionStore = attributionStore;
        this.registry = registry;
        this.abilities = abilities;
    }

    public void cleanupAfterDeath(@lombok.NonNull Player player) {
        cleanupCore(player);

        if (!abilities.isEnabled(CoreAbilities.DEATH_CLEANUP_PARTICIPANTS, player, "death_cleanup_participants"))
            return;

        for (var registered : registry.cleanupParticipants()) {
            try {
                registered.value().cleanupAfterDeath(player);
            } catch (Throwable throwable) {
                log.warn(
                        "[Death] cleanup participant failed: owner={} player={} err={}",
                        registered.ownerId(),
                        player.getName(),
                        throwable.getMessage(),
                        throwable
                );
            }
        }
    }

    private void cleanupCore(Player player) {
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setExp(0.0F);
        player.setLevel(0);
        player.setFoodLevel(20);
        player.setSaturation(5.0F);
        player.setFireTicks(0);
        player.setFreezeTicks(0);
        player.setAbsorptionAmount(0.0D);

        setBaseValue(player, "max_health", DEFAULT_MAX_HEALTH);
        setBaseValue(player, "max_absorption", DEFAULT_MAX_ABSORPTION);
        setBaseValue(player, "knockback_resistance", DEFAULT_KNOCKBACK_RESISTANCE);

        var session = store.get(player);
        if (session != null) {
            session.respawnSecondsRemaining(0);
        }

        attributionStore.clear(player.getUniqueId());
    }

    private void setBaseValue(
            Player player,
            String attributePath,
            double value
    ) {
        var attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(attributePath));
        if (attribute == null) return;

        var instance = player.getAttribute(attribute);
        if (instance == null) return;

        instance.setBaseValue(value);
    }

}
