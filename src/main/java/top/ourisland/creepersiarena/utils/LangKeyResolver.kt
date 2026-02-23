package top.ourisland.creepersiarena.utils

import net.kyori.adventure.text.Component
import top.ourisland.creepersiarena.job.JobId
import top.ourisland.creepersiarena.job.skill.ISkillDefinition
import top.ourisland.creepersiarena.utils.LangKeyResolver.skillBase
import java.util.function.IntFunction
import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * Utility for generating language keys (i18n keys) used by the plugin, and for resolving multi-line "lore" components
 * from those keys via [I18n].
 *
 * This class centralizes naming conventions for language keys to avoid scattered string concatenation across the codebase.
 *
 * ## Key conventions
 * - **Job** keys:
 *   - `cia.job.<jobId>.name`
 *   - `cia.job.<jobId>.lore.<line>`
 * - **Skill** keys:
 *   - `cia.job.<job>.skill.<skill>.name`
 *   - `cia.job.<job>.skill.<skill>.lore.<line>`
 *
 * ## Lore resolving
 * Lore is resolved by querying [I18n.has] for each numbered line key in ascending order starting from `1`.
 * Resolution stops at the first missing key. Each present key is converted into a [Component] using [I18n.langNP].
 *
 * This is a pure static utility and is not instantiable.
 *
 * @author Chiloven945
 * @see I18n
 */
object LangKeyResolver {

    /**
     * Builds the language key for a skill display name.
     *
     * Key format:
     * ```text
     * cia.job.<job>.skill.<skill>.name
     * ```
     *
     * @param skill the skill definition (must not be null)
     * @return the language key for the skill name
     */
    @JvmStatic
    fun skillName(skill: ISkillDefinition): String =
        "${skillBase(skill)}.name"

    /**
     * Builds the base language key prefix for a skill.
     *
     * Key format:
     * ```text
     * cia.job.<job>.skill.<skill>
     * ```
     *
     * This method delegates to [skillBase] using [ISkillDefinition.id].
     *
     * @param skill the skill definition (must not be null)
     * @return the base skill key prefix
     */
    @JvmStatic
    fun skillBase(skill: ISkillDefinition): String =
        skillBase(skill.id())

    /**
     * Builds the base language key prefix for a skill from its string id.
     *
     * Expected `skillId` format: `"job.skill"` (e.g. `"creeper.explode"`).
     *
     * Key format:
     * ```text
     * cia.job.<job>.skill.<skill>
     * ```
     *
     * @param skillId the skill id in `job.skill` format (must not be null)
     * @return the base skill key prefix
     * @throws IllegalArgumentException if `skillId` does not contain a dot separator,
     * starts/ends with a dot, or otherwise cannot be split into `job` and `skill`
     */
    @JvmStatic
    fun skillBase(skillId: String): String {
        val dot = skillId.indexOf('.')
        if (dot <= 0 || dot == skillId.length - 1) {
            throw IllegalArgumentException("Invalid skill id (expected job.skill): $skillId")
        }
        val job = skillId.substring(0, dot)
        val sk = skillId.substring(dot + 1)
        return "cia.job.$job.skill.$sk"
    }

    /**
     * Convenience method to resolve a skill lore with a default maximum of 20 lines.
     *
     * Lines are resolved in order `1..20` using the key format:
     * ```text
     * cia.job.<job>.skill.<skill>.lore.<line>
     * ```
     *
     * Resolution stops early when [I18n.has] returns `false` for a line key.
     *
     * @param skill the skill definition (must not be null)
     * @param loreArgs optional arguments forwarded to [I18n.langNP] for placeholder replacement
     * @return resolved lore lines as [Component]s (possibly empty if the first line key is missing)
     */
    @JvmStatic
    fun resolveSkillLore(skill: ISkillDefinition, vararg loreArgs: Any?): List<Component> =
        resolveLore(20, { i -> skillLore(skill, i) }, *loreArgs)

    /**
     * Generic multi-line lore resolver.
     *
     * Algorithm:
     * 1. Iterate line numbers from `1` to `maxLines` (inclusive).
     * 2. Convert each line number to a key using `lineKey.apply(i)`.
     * 3. Stop at the first key that does not exist (i.e. [I18n.has] returns `false`).
     * 4. Resolve each existing key into a [Component] using [I18n.langNP].
     *
     * If `loreArgs` is empty, this method calls `I18n.langNP(key)` for each line.
     *
     * @param maxLines the maximum number of lines to attempt; values < 1 will be treated as 1
     * @param lineKey a function that maps a 1-based line number to a language key (must not be null)
     * @param loreArgs optional arguments forwarded to [I18n.langNP]
     * @return resolved lore lines as [Component]s (possibly empty)
     */
    @JvmStatic
    fun resolveLore(maxLines: Int, lineKey: IntFunction<String>, vararg loreArgs: Any?): List<Component> {
        val max = maxOf(1, maxLines)

        return IntStream.rangeClosed(1, max)
            .mapToObj { i -> lineKey.apply(i) }
            .takeWhile(I18n::has)
            .map { key ->
                if (loreArgs.isEmpty()) I18n.langNP(key) else I18n.langNP(key, *loreArgs)
            }
            .collect(Collectors.toList())
    }

    /**
     * Builds the language key for a numbered skill lore line.
     *
     * Key format:
     * ```text
     * cia.job.<job>.skill.<skill>.lore.<line>
     * ```
     *
     * @param skill the skill definition
     * @param line 1-based line number
     * @return the language key for the skill lore line
     */
    @JvmStatic
    fun skillLore(skill: ISkillDefinition, line: Int): String =
        "${skillBase(skill)}.lore.$line"

    /**
     * Builds the language key for a job display name.
     *
     * Key format:
     * ```text
     * cia.job.<jobId>.name
     * ```
     *
     * @param jobId the job id (must not be null)
     * @return the language key for the job name
     */
    @JvmStatic
    fun jobName(jobId: JobId): String =
        "${jobBase(jobId)}.name"

    /**
     * Builds the base language key prefix for a job.
     *
     * Key format:
     * ```text
     * cia.job.<jobId>
     * ```
     *
     * @param jobId the job id (must not be null)
     * @return the base job key prefix
     */
    @JvmStatic
    fun jobBase(jobId: JobId): String =
        "cia.job.$jobId"

    /**
     * Convenience method to resolve a job lore with a default maximum of 20 lines.
     *
     * Lines are resolved in order `1..20` using the key format:
     * ```text
     * cia.job.<jobId>.lore.<line>
     * ```
     *
     * Resolution stops early when [I18n.has] returns `false` for a line key.
     *
     * @param jobId the job id (must not be null)
     * @param loreArgs optional arguments forwarded to [I18n.langNP] for placeholder replacement
     * @return resolved lore lines as [Component]s (possibly empty if the first line key is missing)
     */
    @JvmStatic
    fun resolveJobLore(jobId: JobId, vararg loreArgs: Any?): List<Component> =
        resolveLore(20, { i -> jobLore(jobId, i) }, *loreArgs)

    /**
     * Builds the language key for a numbered job lore line.
     *
     * Key format:
     * ```text
     * cia.job.<jobId>.lore.<line>
     * ```
     *
     * @param jobId the job id (must not be null)
     * @param line 1-based line number
     * @return the language key for the job lore line
     */
    @JvmStatic
    fun jobLore(jobId: JobId, line: Int): String =
        "${jobBase(jobId)}.lore.$line"
}
