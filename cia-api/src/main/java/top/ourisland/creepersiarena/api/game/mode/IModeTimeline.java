package top.ourisland.creepersiarena.api.game.mode;

import top.ourisland.creepersiarena.api.game.flow.action.GameAction;
import top.ourisland.creepersiarena.api.game.mode.context.TickContext;

import java.util.List;

/**
 * Produces tick-driven actions for a game mode.
 * <p>
 * A mode timeline is the periodic/temporal counterpart to {@link IModeRules}. Instead of answering one-off policy
 * questions, it examines the current mode state on each game tick (or scheduler step) and emits a list of
 * {@link GameAction}s that the outer flow should perform.
 *
 * <h2>Examples of timeline responsibilities</h2>
 * Implementations may use this contract to:
 * <ul>
 *     <li>advance warmup / round / sudden-death phases</li>
 *     <li>trigger score checks or win-condition transitions</li>
 *     <li>schedule announcements, countdowns or automatic respawn waves</li>
 *     <li>emit arena-control actions without hard-coding them into the generic game loop</li>
 * </ul>
 *
 * <h2>Contract expectations</h2>
 * {@link #tick(TickContext)} should be cheap enough to run frequently. It should describe work by returning actions
 * rather than directly executing all side effects inline, unless the surrounding mode architecture explicitly expects
 * otherwise.
 *
 * @see GameAction
 * @see TickContext
 * @see IModeRules
 */
public interface IModeTimeline {

    /**
     * Returns the mode id this timeline serves.
     *
     * @return namespaced mode id handled by this timeline
     */
    GameModeType type();

    /**
     * Advances the mode timeline for the supplied tick context.
     *
     * @param ctx current tick context for the active game/mode session
     *
     * @return ordered actions the outer flow should execute for this step; an empty list means "no change"
     */
    List<GameAction> tick(TickContext ctx);

    /**
     * Releases mode-owned runtime UI or temporary state when this timeline is no longer active.
     * <p>
     * Core calls this before replacing or ending the active game. Implementations should keep this method cheap and
     * idempotent; it is primarily intended for side-effect-only resources such as bossbars.
     *
     * @param ctx final context for the game session that is being stopped
     */
    default void onStop(TickContext ctx) {
    }

}
