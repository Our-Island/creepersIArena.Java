package top.ourisland.creepersiarena.api.annotation;

import top.ourisland.creepersiarena.api.skill.SkillType;

import java.lang.annotation.*;

/**
 * Marks a class as a discoverable skill definition and declares its immutable catalog metadata.
 * <p>
 * Skills in this project are registered through annotation-driven discovery. {@code @CiaSkillDef} provides the stable
 * metadata required to wire an implementation into the runtime without repeating boilerplate in separate registries.
 * The associated class then supplies triggers, icon generation and executor logic through {@code ISkillDefinition}.
 *
 * <h2>Why this exists</h2>
 * A skill needs more than just executable code. The runtime must know:
 * <ul>
 *     <li>which job owns the skill</li>
 *     <li>which logical category it belongs to</li>
 *     <li>where it should appear in the skill UI</li>
 *     <li>what default cooldown should be seeded before config overrides are applied</li>
 * </ul>
 * This annotation keeps those immutable facts colocated with the skill class.
 *
 * <h2>Id conventions</h2>
 * The current built-in convention is a namespaced job id followed by a dot and a skill-local path, for example
 * {@code cia:creeper.crossbow}. The runtime stores and compares the raw id as-is; language helpers later normalize it
 * into translation-safe segments.
 *
 * @see top.ourisland.creepersiarena.api.CiaExtensionContext#registerAnnotated(org.bukkit.plugin.Plugin, String)
 * @see top.ourisland.creepersiarena.api.metadata.SkillMetadata
 * @see top.ourisland.creepersiarena.api.skill.ISkillDefinition
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CiaSkillDef {

    /**
     * Returns the stable namespaced registry id of the skill.
     * <p>
     * The value must uniquely identify the skill across built-in content and addons. Built-in ids typically follow the
     * pattern {@code <namespace>:<job>.<skill>}.
     *
     * @return globally unique skill id
     */
    String id();

    /**
     * Returns the registry id of the job that owns this skill.
     * <p>
     * The owning job id is used by the catalog to group skills and by the runtime/UI to select the correct skill set
     * for the current player.
     *
     * @return namespaced owning job id
     */
    String job();

    /**
     * Returns the logical category of the skill.
     * <p>
     * The meaning of the category is defined by the surrounding runtime and UI code. Typical uses include separating
     * active skills from passive skills or special built-in mechanics.
     *
     * @return runtime-visible skill type
     */
    SkillType type();

    /**
     * Returns the zero-based slot used by the built-in skill UI.
     * <p>
     * The value expresses preferred placement only; the concrete renderer may still adapt the final inventory layout to
     * fit the current mode or viewer.
     *
     * @return preferred hotbar/UI slot
     */
    int slot();

    /**
     * Returns the default cooldown in seconds.
     * <p>
     * This is the annotation-level seed value used before optional runtime configuration such as {@code skill.yml} is
     * applied.
     *
     * @return default cooldown in seconds
     */
    int defaultCooldown() default 0;

}
