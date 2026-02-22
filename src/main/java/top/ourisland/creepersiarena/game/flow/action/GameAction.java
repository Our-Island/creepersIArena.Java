package top.ourisland.creepersiarena.game.flow.action;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Timeline 每秒产出动作列表；GameFlow 执行它们。
 */
public sealed interface GameAction {

    record Broadcast(Component message) implements GameAction {
    }

    record ToHub(Set<UUID> players) implements GameAction {
    }

    record ToBattle(Set<UUID> players) implements GameAction {
    }

    record ToSpectate(Set<UUID> players, @Nullable Location where) implements GameAction {
    }

    record EndGameAndBackToHub(String reason) implements GameAction {
    }
}
