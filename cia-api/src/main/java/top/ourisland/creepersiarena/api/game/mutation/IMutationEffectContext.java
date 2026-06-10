package top.ourisland.creepersiarena.api.game.mutation;

import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.util.Collection;

/**
 * Runtime services exposed to mutation effects without coupling effects to core internals.
 */
public interface IMutationEffectContext {

    Logger logger();

    MutationClockMode clockMode();

    Collection<Player> targets(MutationTargetScope scope);

}
