package top.ourisland.creepersiarena.util;

import net.kyori.adventure.text.Component;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.skill.SkillDefinition;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility for generating language keys (i18n keys) used by the plugin, and for resolving multi-line "lore" components
 * from those keys via {@link I18n}.
 *
 * <p>This class centralizes naming conventions for language keys to avoid scattered string
 * concatenation across the codebase.</p>
 *
 * <h2>Key conventions</h2>
 * <ul>
 *   <li><b>Job</b> keys:
 *     <ul>
 *       <li>{@code cia.job.<jobId>.name}</li>
 *       <li>{@code cia.job.<jobId>.lore.<line>}</li>
 *     </ul>
 *   </li>
 *   <li><b>Skill</b> keys:
 *     <ul>
 *       <li>{@code cia.job.<job>.skill.<skill>.name}</li>
 *       <li>{@code cia.job.<job>.skill.<skill>.lore.<line>}</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Lore resolving</h2>
 * <p>Lore is resolved by querying {@link I18n#has(String)} for each numbered line key
 * in ascending order starting from {@code 1}. Resolution stops at the first missing key.
 * Each present key is converted into a {@link Component} using {@link I18n#langNP(String, Object...)}.</p>
 *
 * <p>This class is a pure static utility and is not instantiable.</p>
 *
 * @author Chiloven945
 * @see I18n
 */
public final class LangKeyResolver {
    private LangKeyResolver() {
    }

    /**
     * Builds the language key for a skill display name.
     *
     * <p>Key format:
     * <pre>{@code cia.job.<job>.skill.<skill>.name}</pre>
     *
     * @param skill the skill definition (must not be null)
     * @return the language key for the skill name
     */
    public static String skillName(SkillDefinition skill) {
        return skillBase(skill) + ".name";
    }

    /**
     * Builds the base language key prefix for a skill.
     *
     * <p>Key format:
     * <pre>{@code cia.job.<job>.skill.<skill>}</pre>
     *
     * <p>This method delegates to {@link #skillBase(String)} using {@link SkillDefinition#id()}.</p>
     *
     * @param skill the skill definition (must not be null)
     * @return the base skill key prefix
     */
    public static String skillBase(@lombok.NonNull SkillDefinition skill) {
        return skillBase(skill.id());
    }

    /**
     * Builds the base language key prefix for a skill from its string id.
     *
     * <p>Expected {@code skillId} format: {@code "<job>.<skill>"} (e.g. {@code "creeper.explode"}).</p>
     *
     * <p>Key format:
     * <pre>{@code cia.job.<job>.skill.<skill>}</pre>
     *
     * @param skillId the skill id in {@code job.skill} format (must not be null)
     * @return the base skill key prefix
     * @throws IllegalArgumentException if {@code skillId} does not contain a dot separator,
     *                                  starts/ends with a dot, or otherwise cannot be split into {@code job} and {@code skill}
     */
    public static String skillBase(@lombok.NonNull String skillId) {
        int dot = skillId.indexOf('.');
        if (dot <= 0 || dot == skillId.length() - 1) {
            throw new IllegalArgumentException("Invalid skill id (expected job.skill): " + skillId);
        }
        String job = skillId.substring(0, dot);
        String sk = skillId.substring(dot + 1);
        return "cia.job." + job + ".skill." + sk;
    }

    /**
     * Convenience method to resolve a skill lore with a default maximum of 20 lines.
     *
     * <p>Lines are resolved in order {@code 1..20} using the key format:
     * <pre>{@code cia.job.<job>.skill.<skill>.lore.<line>}</pre>
     *
     * <p>Resolution stops early when {@link I18n#has(String)} returns {@code false} for a line key.</p>
     *
     * @param skill the skill definition (must not be null)
     * @param loreArgs optional arguments forwarded to {@link I18n#langNP(String, Object...)} for placeholder replacement
     * @return resolved lore lines as {@link Component}s (possibly empty if the first line key is missing)
     */
    public static List<Component> resolveSkillLore(@lombok.NonNull SkillDefinition skill, Object... loreArgs) {
        return resolveLore(20, i -> skillLore(skill, i), loreArgs);
    }

    /**
     * Generic multi-line lore resolver.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Iterate line numbers from {@code 1} to {@code maxLines} (inclusive).</li>
     *   <li>Convert each line number to a key using {@code lineKey.apply(i)}.</li>
     *   <li>Stop at the first key that does not exist (i.e. {@link I18n#has(String)} returns {@code false}).</li>
     *   <li>Resolve each existing key into a {@link Component} using {@link I18n#langNP(String, Object...)}.</li>
     * </ol>
     *
     * <p>If {@code loreArgs} is null or empty, this method calls {@link I18n#langNP(String)} for each line.</p>
     *
     * @param maxLines the maximum number of lines to attempt; values &lt; 1 will be treated as 1
     * @param lineKey a function that maps a 1-based line number to a language key (must not be null)
     * @param loreArgs optional arguments forwarded to {@link I18n#langNP(String, Object...)}
     * @return resolved lore lines as {@link Component}s (possibly empty)
     */
    public static List<Component> resolveLore(int maxLines, @lombok.NonNull IntFunction<String> lineKey, Object... loreArgs) {
        int max = Math.max(1, maxLines);

        return IntStream.rangeClosed(1, max)
                .mapToObj(lineKey)
                .takeWhile(I18n::has)
                .map(key -> (loreArgs == null || loreArgs.length == 0)
                        ? I18n.langNP(key)
                        : I18n.langNP(key, loreArgs))
                .collect(Collectors.toList());
    }

    /**
     * Builds the language key for a numbered skill lore line.
     *
     * <p>Key format:
     * <pre>{@code cia.job.<job>.skill.<skill>.lore.<line>}</pre>
     *
     * @param skill the skill definition (may be null; but passing null will NPE in
     *              {@link #skillBase(SkillDefinition)})
     * @param line  1-based line number
     * @return the language key for the skill lore line
     */
    public static String skillLore(SkillDefinition skill, int line) {
        return skillBase(skill) + ".lore." + line;
    }

    /**
     * Builds the language key for a job display name.
     *
     * <p>Key format:
     * <pre>{@code cia.job.<jobId>.name}</pre>
     *
     * @param jobId the job id (must not be null)
     * @return the language key for the job name
     */
    public static String jobName(JobId jobId) {
        return jobBase(jobId) + ".name";
    }

    /**
     * Builds the base language key prefix for a job.
     *
     * <p>Key format:
     * <pre>{@code cia.job.<jobId>}</pre>
     *
     * @param jobId the job id (must not be null)
     * @return the base job key prefix
     */
    public static String jobBase(@lombok.NonNull JobId jobId) {
        return "cia.job." + jobId;
    }

    /**
     * Convenience method to resolve a job lore with a default maximum of 20 lines.
     *
     * <p>Lines are resolved in order {@code 1..20} using the key format:
     * <pre>{@code cia.job.<jobId>.lore.<line>}</pre>
     *
     * <p>Resolution stops early when {@link I18n#has(String)} returns {@code false} for a line key.</p>
     *
     * @param jobId the job id (must not be null)
     * @param loreArgs optional arguments forwarded to {@link I18n#langNP(String, Object...)} for placeholder replacement
     * @return resolved lore lines as {@link Component}s (possibly empty if the first line key is missing)
     */
    public static List<Component> resolveJobLore(@lombok.NonNull JobId jobId, Object... loreArgs) {
        return resolveLore(20, i -> jobLore(jobId, i), loreArgs);
    }

    /**
     * Builds the language key for a numbered job lore line.
     *
     * <p>Key format:
     * <pre>{@code cia.job.<jobId>.lore.<line>}</pre>
     *
     * @param jobId the job id (must not be null)
     * @param line 1-based line number
     * @return the language key for the job lore line
     */
    public static String jobLore(JobId jobId, int line) {
        return jobBase(jobId) + ".lore." + line;
    }
}
