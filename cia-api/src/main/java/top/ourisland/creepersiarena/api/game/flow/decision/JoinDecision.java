package top.ourisland.creepersiarena.api.game.flow.decision;

import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

public sealed interface JoinDecision {

    /**
     * Keep the player in the generic hub without attaching them to the active game session.
     */
    record ToHub() implements JoinDecision {

    }

    /**
     * Keep the player in the generic hub, but attach them to the active session as mode-owned audience/waiting data.
     */
    record AttachToHub() implements JoinDecision {

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
