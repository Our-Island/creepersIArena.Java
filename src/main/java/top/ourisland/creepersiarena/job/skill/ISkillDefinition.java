package top.ourisland.creepersiarena.job.skill;

import top.ourisland.creepersiarena.core.component.metadata.SkillMetadata;
import top.ourisland.creepersiarena.job.skill.event.ITrigger;

import java.util.List;

/**
 * Declarative entry point for a single skill implementation.
 * <p>
 * A skill definition ties together two kinds of information:
 * <ul>
 *     <li><strong>Static metadata</strong>: id, owning job, slot, type and default cooldown. In this project those
 *     values are sourced from {@code @CiaSkillDef} and resolved through {@link SkillMetadata}, so implementations do
 *     not need to duplicate immutable registry information in code.</li>
 *     <li><strong>Runtime collaborators</strong>: triggers, icon builder and executor. These collaborators describe how
 *     the skill is surfaced to the player and what happens when it is activated.</li>
 * </ul>
 *
 * <h2>Runtime lifecycle</h2>
 * The skill runtime queries a player's currently selected job, looks up every registered {@code ISkillDefinition}
 * belonging to that job, evaluates {@link #triggers()}, renders the hotbar item via {@link #icon()}, and invokes
 * {@link #executor()} only after trigger and cooldown checks pass.
 *
 * <h2>Implementation guidelines</h2>
 * Implementations are expected to be lightweight descriptors. Any per-player mutable state should live in the
 * {@code SkillStateStore} or on Paper entities / persistent data, not on the definition instance itself. This keeps
 * built-in skills and addon-provided skills safe to reuse as singleton-style catalog entries.
 *
 * @see top.ourisland.creepersiarena.core.component.annotation.CiaSkillDef
 * @see top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry
 * @see top.ourisland.creepersiarena.job.skill.runtime.SkillRuntime
 */
public interface ISkillDefinition {

    /**
     * Returns the stable registry id of this skill.
     * <p>
     * The value comes from the attached {@code @CiaSkillDef} metadata and should be globally unique. Built-in skills
     * use ids such as {@code cia:creeper.crossbow}; addon skills should use their own namespace.
     *
     * @return namespaced skill id resolved from {@link SkillMetadata}
     */
    default String id() {
        return SkillMetadata.of(getClass()).id();
    }

    /**
     * Returns the registry id of the job that owns this skill.
     * <p>
     * This id is used by the skill catalog to group skills per job, by the lobby/battle UI to decide which skills to
     * render for the current player, and by language-key helpers to derive translation prefixes.
     *
     * @return namespaced owning job id resolved from {@link SkillMetadata}
     */
    default String jobId() {
        return SkillMetadata.of(getClass()).job().id();
    }

    /**
     * Returns the skill category understood by the runtime and UI.
     * <p>
     * The type determines how a skill is presented and, depending on the surrounding runtime code, whether it behaves as
     * an active skill, passive skill, or another project-defined category.
     *
     * @return skill type declared in {@code @CiaSkillDef}
     */
    default SkillType type() {
        return SkillMetadata.of(getClass()).type();
    }

    /**
     * Returns the preferred hotbar slot used when rendering this skill.
     * <p>
     * Slots are interpreted by the skill UI layer. The current built-in implementation treats this as a zero-based slot
     * index and may override the player's inventory view accordingly.
     *
     * @return zero-based UI slot resolved from {@link SkillMetadata}
     */
    default int uiSlot() {
        return SkillMetadata.of(getClass()).slot();
    }

    /**
     * Returns the annotation-declared cooldown in seconds.
     * <p>
     * This is the catalog default rather than the final effective cooldown. Runtime configuration such as
     * {@code skill.yml} may override the value before it is shown to players or enforced at execution time.
     *
     * @return default cooldown in seconds
     */
    default int cooldownSeconds() {
        return SkillMetadata.of(getClass()).defaultCooldown();
    }

    /**
     * Returns the ordered trigger chain for this skill.
     * <p>
     * A trigger list usually contains one or more predicates that observe player actions or tick events. The skill
     * runtime evaluates them in order to decide whether the skill should fire for a given event.
     *
     * @return ordered triggers used to match runtime events
     */
    List<ITrigger> triggers();

    /**
     * Returns the icon builder responsible for the player's visible skill item.
     * <p>
     * The returned builder may render cooldown text, passive-state hints, or job-specific styling based on the viewer.
     * The runtime may call it repeatedly, so implementations should avoid expensive work where possible.
     *
     * @return per-player icon builder for this skill
     */
    ISkillIcon icon();

    /**
     * Returns the executor that performs the actual gameplay effect.
     * <p>
     * The executor is invoked only after the runtime has already matched the trigger and accepted the activation. It is
     * therefore responsible for the concrete Paper-side behaviour of the skill: spawning entities, applying effects,
     * mutating temporary state, dealing damage, and so on.
     *
     * @return gameplay executor for this skill
     */
    ISkillExecutor executor();

}
