package top.ourisland.creepersiarena.game.mutation.effect.acceleratedtime;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.utils.AttributeUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

final class AcceleratedTimeAttributeController {

    private final NamespacedKey movementSpeedKey;
    private final Logger logger;
    private final Set<UUID> appliedPlayers = new HashSet<>();

    private double appliedAmount = Double.NaN;

    public AcceleratedTimeAttributeController(
            Plugin plugin,
            Logger logger
    ) {
        this.movementSpeedKey = new NamespacedKey(plugin, "mutation_accelerated_time_speed");
        this.logger = logger;
    }

    public void ensureApplied(
            Collection<Player> targets,
            double amount
    ) {
        if (targets == null || amount <= 0.0D) return;
        if (Double.isNaN(appliedAmount) || Double.compare(appliedAmount, amount) != 0) {
            clearAll();
            appliedAmount = amount;
        }

        var current = new HashSet<UUID>();
        for (var player : targets) {
            if (player == null || !player.isOnline()) continue;
            current.add(player.getUniqueId());
            ensureApplied(player, amount);
        }

        for (var playerId : new HashSet<>(appliedPlayers)) {
            if (current.contains(playerId)) continue;
            var player = Bukkit.getPlayer(playerId);
            if (player != null) remove(player);
            appliedPlayers.remove(playerId);
        }
    }

    public void clear(Player player) {
        if (player == null) return;
        remove(player);
        appliedPlayers.remove(player.getUniqueId());
    }

    private void remove(Player player) {
        AttributeInstance instance = movementSpeedInstance(player);
        if (instance == null) return;
        for (var modifier : Set.copyOf(instance.getModifiers())) {
            if (movementSpeedKey.equals(modifier.getKey())) instance.removeModifier(modifier);
        }
    }

    private AttributeInstance movementSpeedInstance(Player player) {
        var attribute = AttributeUtils.attributeOrNull("movement_speed", "generic_movement_speed");
        if (attribute == null) return null;
        return player.getAttribute(attribute);
    }

    public void clearAll() {
        for (var playerId : new HashSet<>(appliedPlayers)) {
            var player = Bukkit.getPlayer(playerId);
            if (player != null) remove(player);
        }
        appliedPlayers.clear();
        appliedAmount = Double.NaN;
    }

    private void ensureApplied(
            Player player,
            double amount
    ) {
        AttributeInstance instance = movementSpeedInstance(player);
        if (instance == null) return;

        boolean correct = false;
        AttributeModifier stale = null;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (!movementSpeedKey.equals(modifier.getKey())) continue;
            correct = Double.compare(modifier.getAmount(), amount) == 0
                    && modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER;
            if (!correct) stale = modifier;
            break;
        }
        if (stale != null) instance.removeModifier(stale);

        if (!correct) {
            try {
                instance.addModifier(new AttributeModifier(
                        movementSpeedKey,
                        amount,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.ANY
                ));
            } catch (IllegalArgumentException ignored) {
                remove(player);
                instance.addModifier(new AttributeModifier(
                        movementSpeedKey,
                        amount,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.ANY
                ));
            } catch (Throwable t) {
                logger.warn("[Mutation] Failed to add accelerated-time speed modifier for {}: {}", player.getName(), t.getMessage());
                return;
            }
        }

        appliedPlayers.add(player.getUniqueId());
    }

}
