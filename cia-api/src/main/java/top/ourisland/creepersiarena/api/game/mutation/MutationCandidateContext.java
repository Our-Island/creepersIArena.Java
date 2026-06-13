package top.ourisland.creepersiarena.api.game.mutation;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.arena.ArenaId;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;

import java.util.Collection;
import java.util.List;

/**
 * Facts used by the mutation engine before selecting an effect.
 */
public record MutationCandidateContext(
        @Nullable GameSession game,
        @Nullable GameModeId modeId,
        @Nullable ArenaId arenaId,
        int idleTicks,
        Collection<Player> activeTargets
) {

    public MutationCandidateContext {
        activeTargets = activeTargets == null
                ? List.of()
                : List.copyOf(activeTargets);
    }

}
