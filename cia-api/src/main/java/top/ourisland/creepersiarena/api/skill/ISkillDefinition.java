package top.ourisland.creepersiarena.api.skill;

import top.ourisland.creepersiarena.api.metadata.SkillMetadata;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;

import java.util.List;

/**
 * Declarative description of a single skill that can be registered into the CreepersIArena skill catalog.
 * <p>
 * An {@code ISkillDefinition} is intentionally split between two kinds of information:
 * <ul>
 *     <li><strong>Static registry metadata</strong>, such as the skill id, owning job id, type, UI slot and default
 *     cooldown. In this project these values are derived from {@code @CiaSkillDef} through
 *     {@link SkillMetadata}, so concrete skill classes do not need to duplicate immutable registry data in fields or
 *     constructors.</li>
 *     <li><strong>Runtime collaborators</strong>, namely {@link #triggers()}, {@link #icon()} and
 *     {@link #executor()}. Together these collaborators define how the skill is discovered by the runtime, how it is
 *     presented to players, and what gameplay effect it performs once activation succeeds.</li>
 * </ul>
 *
 * <h2>How the runtime uses a definition</h2>
 * The surrounding runtime treats a definition as a lightweight singleton-style descriptor:
 * <ol>
 *     <li>It resolves the owning job via {@link #jobId()} and only considers skills belonging to the player's current
 *     job.</li>
 *     <li>It evaluates the ordered {@linkplain #triggers() trigger list} against incoming player actions or synthetic
 *     tick events.</li>
 *     <li>It uses {@link #icon()} to render the visible hotbar item shown to the player, including cooldown and passive
 *     hints.</li>
 *     <li>When a trigger matches and outer cooldown checks pass, it delegates to {@link #executor()} for the final
 *     gameplay activation.</li>
 * </ol>
 *
 * <h2>Triggers do not guarantee activation</h2>
 * A trigger only means that the runtime should <em>attempt</em> activation. Many skills still need additional runtime
 * checks that cannot be expressed cleanly at trigger level alone, such as:
 * <ul>
 *     <li>whether a recent valid target is still available,</li>
 *     <li>whether a teleport anchor or projectile marker exists,</li>
 *     <li>whether several sibling skills are all cooling down,</li>
 *     <li>or whether the world / entity state still allows the effect to be applied.</li>
 * </ul>
 * Those final checks belong to the executor. A definition should therefore be read as “this is how a skill is wired”,
 * not “this trigger alone guarantees the skill will fire”.
 *
 * <h2>State and lifecycle expectations</h2>
 * Implementations should remain stateless descriptors. Per-player counters, temporary flags, cooldown bookkeeping and
 * similar mutable values should be stored in runtime systems such as the skill state store, player/entity persistent
 * data containers, or other dedicated services. This keeps definitions safe to register once and reuse across the
 * whole plugin, including future extension-provided skills.
 *
 * @see top.ourisland.creepersiarena.api.annotation.CiaSkillDef
 *
 *
 */
public interface ISkillDefinition {

    /**
     * Returns the stable runtime id of this skill.
     * <p>
     * The value comes from the attached {@code @CiaSkillDef} metadata and must be globally unique inside the runtime
     * catalog. Built-in skills use ids such as {@code cia:creeper.crossbow}; extension skills are expected to use their
     * own namespace to avoid collisions.
     *
     * @return namespaced skill id resolved from {@link SkillMetadata}
     */
    default String id() {
        return SkillMetadata.of(getClass()).id();
    }

    /**
     * Returns the runtime id of the job that owns this skill.
     * <p>
     * This id is used by the skill catalog to group definitions per job, by lobby / in-game UI code to decide which
     * skills to render for a player, and by language-key helpers to derive translation prefixes.
     *
     * @return namespaced owning job id resolved from {@link SkillMetadata}
     */
    default String jobId() {
        return SkillMetadata.of(getClass()).job().id();
    }

    /**
     * Returns the skill category understood by the runtime and UI.
     * <p>
     * The type controls how the skill is surfaced and interpreted by surrounding systems, for example whether it is an
     * active hotbar skill, a passive/tick-driven skill, or another project-defined category.
     *
     * @return skill type declared in {@code @CiaSkillDef}
     */
    default SkillType type() {
        return SkillMetadata.of(getClass()).type();
    }

    /**
     * Returns the preferred hotbar slot used when presenting this skill.
     * <p>
     * Slots are interpreted by the skill UI layer. In the current built-in implementation this is treated as a
     * zero-based hotbar slot index that the renderer may fill or overwrite when presenting skill items.
     *
     * @return zero-based UI slot resolved from {@link SkillMetadata}
     */
    default int uiSlot() {
        return SkillMetadata.of(getClass()).slot();
    }

    /**
     * Returns the annotation-declared default cooldown in seconds.
     * <p>
     * This is the registry default rather than the final effective cooldown. Runtime configuration such as
     * {@code skill.yml} may override the value before it is displayed to players or enforced by the runtime.
     *
     * @return default cooldown in seconds
     */
    default int cooldownSeconds() {
        return SkillMetadata.of(getClass()).defaultCooldown();
    }

    /**
     * Returns the ordered trigger chain that makes this skill eligible for activation.
     * <p>
     * Each trigger observes some runtime signal, such as a player interaction, weapon use, projectile event or periodic
     * tick callback. The runtime evaluates the list to decide whether this definition should be considered for the
     * current event.
     *
     * <p>A matched trigger means “activation should be attempted”, not “activation must succeed”. Final gameplay
     * checks may still be performed later by {@link #executor()}.
     *
     * @return ordered triggers used by the runtime to detect possible activations
     */
    List<ITrigger> triggers();

    /**
     * Returns the icon builder responsible for the skill's visible item representation.
     * <p>
     * The icon builder may render cooldown text, passive-state hints, job-specific styling or other viewer-dependent UI
     * details. The runtime may call it frequently while refreshing a player's hotbar, so implementations should keep
     * the logic deterministic and reasonably cheap.
     *
     * @return icon builder used to render this skill for a player
     */
    ISkillIcon icon();

    /**
     * Returns the executor responsible for the actual Paper-side gameplay effect.
     * <p>
     * The executor is the final activation step. It may spawn entities, apply potion effects, teleport players,
     * manipulate temporary state, deal damage, or reject activation if additional gameplay conditions are not met.
     *
     * <p>If activation should be cancelled without consuming cooldown, the executor is expected to signal that via
     * {@link top.ourisland.creepersiarena.api.skill.runtime.SkillActivationRejectedException}. Returning normally means
     * the skill was accepted as successfully activated.
     *
     * @return gameplay executor for this skill
     */
    ISkillExecutor executor();

}
