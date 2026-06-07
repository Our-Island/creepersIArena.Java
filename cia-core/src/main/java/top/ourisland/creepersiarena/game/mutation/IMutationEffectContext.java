package top.ourisland.creepersiarena.game.mutation;

import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.util.Collection;

/**
 * Runtime services exposed to mutation effects without coupling effects back to the global state machine internals.
 */
public interface IMutationEffectContext {

    MutationConfig config();

    Logger logger();

    Collection<Player> targets(MutationTargetScope scope);

}
