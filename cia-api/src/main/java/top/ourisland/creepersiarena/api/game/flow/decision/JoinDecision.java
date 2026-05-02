package top.ourisland.creepersiarena.api.game.flow.decision;

import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

public sealed interface JoinDecision {

    record ToHub() implements JoinDecision {

    }

    record ToSpectate(
            @Nullable Location where
    ) implements JoinDecision {

    }

    /**
     * Directly move the player into the active game through the active mode's player-flow hooks.
     */
    record EnterGame() implements JoinDecision {

    }

}
