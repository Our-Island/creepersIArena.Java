package top.ourisland.creepersiarena.api.game.mutation;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.game.GameSession;

import java.util.Collection;
import java.util.List;

/**
 * Facts used by the mutation engine before selecting an effect.
 */
public record MutationCandidateContext(
        @Nullable GameSession game,
        String modeId,
        String arenaId,
        int idleTicks,
        Collection<Player> activeTargets
) {

    public MutationCandidateContext {
        modeId = modeId == null
                ? ""
                : modeId;
        arenaId = arenaId == null
                ? ""
                : arenaId;
        activeTargets = activeTargets == null
                ? List.of()
                : List.copyOf(activeTargets);
    }

}
