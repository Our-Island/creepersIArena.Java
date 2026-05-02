package top.ourisland.creepersiarena.api.game.mode;

import top.ourisland.creepersiarena.api.game.flow.decision.JoinDecision;
import top.ourisland.creepersiarena.api.game.flow.decision.RespawnDecision;
import top.ourisland.creepersiarena.api.game.mode.context.JoinContext;
import top.ourisland.creepersiarena.api.game.mode.context.LeaveContext;
import top.ourisland.creepersiarena.api.game.mode.context.RespawnContext;

/**
 * Encapsulates the policy decisions of a game mode.
 * <p>
 * Where {@link IGameMode} identifies a mode and {@link IModeTimeline} drives periodic transitions, {@code IModeRules}
 * answers discrete policy questions asked by the surrounding flow layer: may a player join, what should happen when a
 * player leaves, and how should respawn be handled for this mode.
 *
 * <h2>Why rules are separate</h2>
 * Pulling these decisions into a dedicated contract keeps the arena/game flow code generic. The outer flow does not
 * need to hard-code battle-specific or steal-specific behaviour; it asks the currently active mode rules object and
 * then applies the returned decision.
 *
 * <h2>Expected usage</h2>
 * Implementations are usually stateless or lightly stateful helpers keyed by a single {@link #type()}. They should
 * return deterministic decisions based on the supplied contexts and should avoid directly mutating unrelated runtime
 * systems unless the contract explicitly expects side effects (such as cleanup in {@link #onLeave(LeaveContext)}).
 *
 * @see IGameMode
 * @see IModeTimeline
 * @see JoinDecision
 * @see RespawnDecision
 */
public interface IModeRules {

    /**
     * Returns the mode id this rules object serves.
     *
     * @return namespaced mode id handled by this rules implementation
     */
    GameModeType type();

    /**
     * Evaluates whether and how a player may join while this mode is active.
     *
     * @param ctx join-time context containing the player, session and mode-specific environment data
     *
     * @return decision object describing whether the join is accepted and any follow-up action to apply
     */
    JoinDecision onJoin(JoinContext ctx);

    /**
     * Performs mode-specific cleanup or bookkeeping when a player leaves.
     * <p>
     * Unlike {@link #onJoin(JoinContext)} and {@link #onRespawn(RespawnContext)}, this method communicates by side
     * effect rather than a returned decision object.
     *
     * @param ctx leave-time context for the departing player
     */
    void onLeave(LeaveContext ctx);

    /**
     * Evaluates how respawn should be handled for the current mode.
     * <p>
     * Typical outcomes include an immediate return to play, moving the player into a death/spectator flow, or routing
     * them back to lobby logic. The concrete meaning is encoded by the returned {@link RespawnDecision}.
     *
     * @param ctx respawn-time context containing the player and current game state
     *
     * @return mode-specific respawn decision
     */
    RespawnDecision onRespawn(RespawnContext ctx);

}
