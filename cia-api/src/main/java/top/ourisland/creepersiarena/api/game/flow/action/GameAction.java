package top.ourisland.creepersiarena.api.game.flow.action;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public sealed interface GameAction {

    record Broadcast(
            Component message
    ) implements GameAction {

    }

    record ToHub(
            Set<UUID> players
    ) implements GameAction {

    }

    record EnterGame(
            Set<UUID> players
    ) implements GameAction {

    }

    record ToSpectate(
            Set<UUID> players,
            @Nullable Location where
    ) implements GameAction {

    }

    record EndGameAndBackToHub(
            String reason
    ) implements GameAction {

    }

}
