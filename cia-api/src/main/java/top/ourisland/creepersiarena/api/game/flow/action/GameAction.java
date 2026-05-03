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

        public ToHub {
            players = players == null ? Set.of() : Set.copyOf(players);
        }

    }

    record EnterGame(
            Set<UUID> players
    ) implements GameAction {

        public EnterGame {
            players = players == null ? Set.of() : Set.copyOf(players);
        }

    }

    record ToSpectate(
            Set<UUID> players,
            @Nullable Location where
    ) implements GameAction {

        public ToSpectate {
            players = players == null ? Set.of() : Set.copyOf(players);
        }

    }

    record EndGame(
            String reason
    ) implements GameAction {

    }

    record RotateArena(
            String reason
    ) implements GameAction {

    }

    record EndGameAndBackToHub(
            Set<UUID> players,
            String reason
    ) implements GameAction {

        public EndGameAndBackToHub(String reason) {
            this(Set.of(), reason);
        }

        public EndGameAndBackToHub {
            players = players == null ? Set.of() : Set.copyOf(players);
        }

    }

}
