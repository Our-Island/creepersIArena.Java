package top.ourisland.creepersiarena.defaultcontent.game.mutation.acceleratedtime;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.core.utils.AttributeUtils;

import java.util.*;

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
            @Nullable Collection<Player> targets,
            double amount
    ) {
        if (targets == null) return;

        if (amount <= 0.0D) {
            clearAll();
            return;
        }

        boolean amountChanged = Double.isNaN(appliedAmount) || Double.compare(appliedAmount, amount) != 0;
        if (amountChanged) {
            new HashSet<>(appliedPlayers).stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(this::remove);

            appliedPlayers.clear();
            appliedAmount = amount;
        }

        var current = new HashSet<UUID>();
        targets.stream()
                .filter(player -> player != null && player.isOnline())
                .forEach(player -> {
                    current.add(player.getUniqueId());
                    ensureApplied(player, amount);
                });

        new HashSet<>(appliedPlayers).stream()
                .filter(playerId -> !current.contains(playerId))
                .forEach(playerId -> {
                    var player = Bukkit.getPlayer(playerId);
                    if (player != null) remove(player);
                    appliedPlayers.remove(playerId);
                });
    }

    public void clear(Player player) {
        if (player == null) return;
        remove(player);
        appliedPlayers.remove(player.getUniqueId());
    }

    private void remove(Player player) {
        AttributeInstance instance = movementSpeedInstance(player);
        if (instance == null) return;
        Set.copyOf(instance.getModifiers()).stream()
                .filter(modifier -> movementSpeedKey.equals(modifier.getKey()))
                .forEach(instance::removeModifier);
    }

    private AttributeInstance movementSpeedInstance(Player player) {
        var attribute = AttributeUtils.attributeOrNull("movement_speed", "generic_movement_speed");
        if (attribute == null) return null;
        return player.getAttribute(attribute);
    }

    public void clearAll() {
        new HashSet<>(appliedPlayers).stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(this::remove);

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
