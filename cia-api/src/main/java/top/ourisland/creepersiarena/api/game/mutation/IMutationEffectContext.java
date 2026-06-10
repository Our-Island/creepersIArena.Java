package top.ourisland.creepersiarena.api.game.mutation;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.GameSession;

import java.util.Collection;

/**
 * Runtime services exposed to mutation effects without coupling effects to core internals.
 */
public interface IMutationEffectContext {

    Logger logger();

    default @Nullable World world() {
        var game = game();
        return game == null || game.arena() == null ? null : game.arena().world();
    }

    default @Nullable GameSession game() {
        return null;
    }

    MutationClockMode clockMode();

    Collection<Player> targets(MutationTargetScope scope);

}
