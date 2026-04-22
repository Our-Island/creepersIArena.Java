package top.ourisland.creepersiarena.job.skill;

import top.ourisland.creepersiarena.job.skill.event.SkillContext;
import top.ourisland.creepersiarena.job.skill.runtime.SkillStateStore;

/**
 * Functional contract for performing a skill's gameplay effect.
 * <p>
 * An {@code ISkillExecutor} is the imperative half of a skill definition. Triggers answer <em>when</em> a skill should
 * run; the executor defines <em>what</em> the skill actually does on Paper once activation has been accepted.
 *
 * <h2>When executors run</h2>
 * The surrounding skill runtime is responsible for event matching, ownership lookup and cooldown/state validation.
 * By the time {@link #execute(SkillContext, SkillStateStore)} is called, the invocation has already been accepted as a
 * legitimate activation for the associated skill.
 *
 * <h2>Where state should live</h2>
 * Executors should not rely on mutating the definition instance. Instead, transient values should be written to the
 * provided {@link SkillStateStore}, to entity persistent data, or to other runtime services explicitly designed for
 * per-player / per-skill state.
 *
 * <h2>Error handling</h2>
 * The contract intentionally does not declare checked exceptions. Implementations should fail fast and keep side effects
 * consistent, because an exception during execution can interrupt the current event pipeline.
 *
 * @see SkillContext
 * @see SkillStateStore
 * @see ISkillDefinition
 */
@FunctionalInterface
public interface ISkillExecutor {

    /**
     * Executes the accepted skill activation.
     *
     * @param ctx immutable invocation context containing the triggering player, triggering event and resolved skill
     *            information
     * @param store mutable runtime store for cooldown data, counters and other transient skill-scoped values
     */
    void execute(SkillContext ctx, SkillStateStore store);

}
