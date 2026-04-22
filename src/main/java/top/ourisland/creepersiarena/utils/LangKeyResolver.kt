package top.ourisland.creepersiarena.utils

import net.kyori.adventure.text.Component
import top.ourisland.creepersiarena.job.JobId
import top.ourisland.creepersiarena.job.skill.ISkillDefinition
import top.ourisland.creepersiarena.utils.LangKeyResolver.normalizeLangSegment
import top.ourisland.creepersiarena.utils.LangKeyResolver.skillBase
import java.util.function.IntFunction
import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * Utility for generating language keys (i18n keys) used by the plugin and for resolving multi-line lore components from
 * those keys via [I18n].
 *
 * This object centralizes translation-key naming rules so job/skill UI code does not have to hand-roll string
 * concatenation. It also defines how runtime registry ids such as `cia:creeper` or `addon:mage.fireball` are mapped to
 * stable translation segments.
 *
 * ## Key conventions
 * - **Job** keys:
 *   - `cia.job.<jobId>.name`
 *   - `cia.job.<jobId>.lore.<line>`
 * - **Skill** keys:
 *   - `cia.job.<job>.skill.<skill>.name`
 *   - `cia.job.<job>.skill.<skill>.lore.<line>`
 *
 * ## Runtime id normalization
 * Runtime ids are not inserted into translation keys verbatim.
 * [normalizeLangSegment] converts namespaced ids into dot-safe segments:
 * - built-in ids in the `cia` namespace drop the leading namespace to avoid redundant `cia.job.cia.*` keys
 * - third-party namespaces are preserved, so addon content remains distinct
 *
 * Examples:
 * - `cia:creeper` -> `creeper`
 * - `cia:creeper.crossbow` -> `creeper.crossbow` when used as a raw segment
 * - `addon:mage` -> `addon.mage`
 *
 * ## Skill id parsing
 * Skill ids are currently expected to follow the convention `<jobId>.<skillPath>`, where `<jobId>` itself may be
 * namespaced (for example `cia:creeper.crossbow` or `addon:mage.fireball`). [skillBase] splits on the **last** dot so
 * the left-hand side becomes the runtime job id and the right-hand side becomes the skill-local segment.
 *
 * ## Lore resolving
 * Lore is resolved by querying [I18n.has] for numbered line keys in ascending order starting from `1`. Resolution stops
 * at the first missing line. Each present line is converted into a [Component] via [I18n.langNP].
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
     * @param skill skill definition whose runtime id should be translated
     * @return language key for the skill name
     */
    @JvmStatic
    fun skillName(skill: ISkillDefinition): String =
        "${skillBase(skill)}.name"

    /**
     * Builds the base translation prefix for a skill definition.
     *
     * This overload simply delegates to [skillBase] using [ISkillDefinition.id].
     *
     * @param skill skill definition
     * @return base translation-key prefix for the skill
     */
    @JvmStatic
    fun skillBase(skill: ISkillDefinition): String =
        skillBase(skill.id())

    /**
     * Builds the base translation prefix for a raw skill id.
     *
     * Expected input format is `<jobId>.<skillPath>`, where `<jobId>` may itself be namespaced. Examples:
     * - `cia:creeper.crossbow`
     * - `cia:moison.shadowstep`
     * - `addon:mage.fireball`
     *
     * Parsing uses the **last** dot in the string. Everything before that dot is treated as the runtime job id and
     * normalized via [normalizeLangSegment]; everything after the dot becomes the skill-local segment.
     *
     * Resulting key format:
     * ```text
     * cia.job.<job>.skill.<skill>
     * ```
     *
     * @param skillId runtime skill id in `<jobId>.<skillPath>` form
     * @return base skill translation-key prefix
     * @throws IllegalArgumentException if [skillId] does not contain a usable dot separator
     */
    @JvmStatic
    fun skillBase(skillId: String): String {
        val dot = skillId.lastIndexOf('.')
        if (dot <= 0 || dot == skillId.length - 1) {
            throw IllegalArgumentException("Invalid skill id (expected <jobId>.<skillPath>): $skillId")
        }
        val job = normalizeLangSegment(skillId.substring(0, dot))
        val sk = normalizeLangSegment(skillId.substring(dot + 1))
        return "cia.job.$job.skill.$sk"
    }

    /**
     * Resolves all translated lore lines for a skill, using a default maximum of 20 lines.
     *
     * Keys are checked in ascending order starting at line `1`:
     * ```text
     * cia.job.<job>.skill.<skill>.lore.<line>
     * ```
     * Resolution stops at the first missing key.
     *
     * @param skill skill definition
     * @param loreArgs optional [I18n] formatting arguments forwarded to each resolved line
     * @return translated lore components, possibly empty when the first line key is missing
     */
    @JvmStatic
    fun resolveSkillLore(skill: ISkillDefinition, vararg loreArgs: Any?): List<Component> =
        resolveLore(20, { i -> skillLore(skill, i) }, *loreArgs)

    /**
     * Generic numbered-lore resolver.
     *
     * Algorithm:
     * 1. Normalize [maxLines] so it is at least `1`.
     * 2. Generate numbered keys from `1..maxLines` using [lineKey].
     * 3. Stop at the first missing key according to [I18n.has].
     * 4. Convert each present key into a [Component] using [I18n.langNP].
     *
     * This contract matches the plugin's current resource layout, where lore lines are stored as consecutive numbered
     * keys with no gaps.
     *
     * @param maxLines maximum number of lines to probe; values below `1` are treated as `1`
     * @param lineKey function producing the key for each 1-based line number
     * @param loreArgs optional Adventure / MessageFormat arguments
     * @return translated lore components in display order
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
     * @param skill skill definition
     * @param line 1-based lore line number
     * @return language key in the form `cia.job.<job>.skill.<skill>.lore.<line>`
     */
    @JvmStatic
    fun skillLore(skill: ISkillDefinition, line: Int): String =
        "${skillBase(skill)}.lore.$line"

    /**
     * Builds the language key for a job display name.
     *
     * @param jobId runtime job id
     * @return language key in the form `cia.job.<job>.name`
     */
    @JvmStatic
    fun jobName(jobId: JobId): String =
        "${jobBase(jobId)}.name"

    /**
     * Builds the base translation prefix for a job id.
     *
     * Built-in ids in the `cia` namespace collapse to `cia.job.<path>`, while addon namespaces are preserved as
     * `cia.job.<namespace>.<path>`.
     *
     * @param jobId runtime job id
     * @return base translation-key prefix for the job
     */
    @JvmStatic
    fun jobBase(jobId: JobId): String =
        "cia.job.${normalizeLangSegment(jobId.id())}"

    /**
     * Resolves all translated lore lines for a job, using a default maximum of 20 lines.
     *
     * @param jobId runtime job id
     * @param loreArgs optional [I18n] formatting arguments forwarded to each resolved line
     * @return translated lore components, possibly empty when the first line key is missing
     */
    @JvmStatic
    fun resolveJobLore(jobId: JobId, vararg loreArgs: Any?): List<Component> =
        resolveLore(20, { i -> jobLore(jobId, i) }, *loreArgs)

    /**
     * Builds the language key for a numbered job lore line.
     *
     * @param jobId runtime job id
     * @param line 1-based lore line number
     * @return language key in the form `cia.job.<job>.lore.<line>`
     */
    @JvmStatic
    fun jobLore(jobId: JobId, line: Int): String =
        "${jobBase(jobId)}.lore.$line"

    /**
     * Converts a runtime id segment into a translation-safe segment.
     *
     * Built-in `cia:` ids drop their namespace so built-in keys stay concise, while third-party namespaces are preserved
     * to avoid collisions between addon content.
     *
     * Examples:
     * - `cia:creeper` -> `creeper`
     * - `cia:creeper.crossbow` -> `creeper.crossbow`
     * - `addon:mage` -> `addon.mage`
     *
     * @param raw raw runtime id or segment
     * @return normalized segment ready to be embedded in a translation key
     */
    @JvmStatic
    fun normalizeLangSegment(raw: String): String {
        val trimmed = raw.trim().lowercase()
        val colon = trimmed.indexOf(':')
        if (colon <= 0) return trimmed.replace(':', '.')
        val namespace = trimmed.substring(0, colon)
        val path = trimmed.substring(colon + 1)
        return if (namespace == "cia") path.replace(':', '.') else "$namespace.${path.replace(':', '.')}"
    }

}
