package top.ourisland.creepersiarena.api.skill;

import top.ourisland.creepersiarena.api.skill.event.SkillContext;
import top.ourisland.creepersiarena.api.skill.runtime.ISkillStateStore;

/**
 * Functional contract for performing the gameplay side of a skill activation.
 * <p>
 * An {@code ISkillExecutor} is the imperative half of an {@link ISkillDefinition}. Triggers describe <em>when</em> the
 * runtime should attempt activation; the executor defines <em>what</em> actually happens on Paper once the runtime has
 * selected the skill as a candidate for execution.
 *
 * <h2>Where executors sit in the activation pipeline</h2>
 * By the time {@link #execute(SkillContext, ISkillStateStore)} is invoked, the runtime has already:
 * <ul>
 *     <li>resolved the player's current job and skill definition,</li>
 *     <li>matched the outer trigger chain for the current event,</li>
 *     <li>performed generic runtime-side cooldown / ownership checks,</li>
 *     <li>and assembled a {@link SkillContext} describing the activation attempt.</li>
 * </ul>
 * <p>
 * The executor is therefore responsible for the <strong>final gameplay decision</strong>: it may perform the effect,
 * or it may reject the activation if more specific conditions are required than the trigger system can express.
 *
 * <h2>Conditional activation rule</h2>
 * Not every matched trigger should consume cooldown. Some skills only become truly valid after inspecting live state
 * inside the executor, for example:
 * <ul>
 *     <li>a locked target must still exist and remain in range,</li>
 *     <li>a teleport anchor / projectile marker must be present,</li>
 *     <li>several sibling skills must all currently be cooling down,</li>
 *     <li>or a world/entity condition must still hold at execution time.</li>
 * </ul>
 * <p>
 * The contract is therefore:
 * <ul>
 *     <li><strong>Return normally</strong>: activation succeeded and the runtime may treat the skill as consumed.</li>
 *     <li><strong>Throw</strong> {@link top.ourisland.creepersiarena.api.skill.runtime.SkillActivationRejectedException}:
 *     activation was intentionally cancelled and cooldown must not be applied.</li>
 *     <li><strong>Throw any other exception</strong>: execution failed unexpectedly and should be treated as an actual
 *     error, not a normal gameplay rejection.</li>
 * </ul>
 *
 * <h2>State ownership</h2>
 * Executors should not rely on mutating the definition instance itself. Temporary counters, timestamps, chained skill
 * state and similar values should be written to the provided {@link ISkillStateStore}, to player/entity persistent data,
 * or to another runtime service designed for mutable per-player state.
 *
 * <h2>Implementation scope</h2>
 * Typical executor work includes spawning or tagging entities, applying potion effects, dealing damage, moving players,
 * scheduling short-lived follow-up tasks, and updating transient state needed by later hits or ticks.
 *
 * @see SkillContext
 * @see ISkillStateStore
 * @see ISkillDefinition
 * @see top.ourisland.creepersiarena.api.skill.runtime.SkillActivationRejectedException
 *
 */
@FunctionalInterface
public interface ISkillExecutor {

    /**
     * Performs the final gameplay execution for a skill activation attempt.
     * <p>
     * A normal return means that activation succeeded. If the activation must be cancelled without consuming cooldown,
     * implementations should throw
     * {@link top.ourisland.creepersiarena.api.skill.runtime.SkillActivationRejectedException} instead of silently
     * returning.
     *
     * @param ctx   immutable invocation context containing the triggering player, triggering event and resolved skill
     *              information
     * @param store mutable runtime store for cooldown bookkeeping, counters and other transient skill-scoped values
     *
     * @throws top.ourisland.creepersiarena.api.skill.runtime.SkillActivationRejectedException when activation is
     *                                                                                         intentionally rejected as
     *                                                                                         a normal gameplay
     *                                                                                         outcome
     */
    void execute(SkillContext ctx, ISkillStateStore store);

}
