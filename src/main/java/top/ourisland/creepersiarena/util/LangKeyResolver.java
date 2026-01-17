package top.ourisland.creepersiarena.util;

import net.kyori.adventure.text.Component;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.skill.SkillDefinition;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class LangKeyResolver {
    private LangKeyResolver() {
    }

    // ------------------------
    // Skill keys
    // ------------------------

    /**
     * cia.job.<job>.skill.<sk>.name
     */
    public static String skillName(SkillDefinition skill) {
        return skillBase(skill) + ".name";
    }

    /**
     * cia.job.<job>.skill.<sk>
     */
    public static String skillBase(SkillDefinition skill) {
        Objects.requireNonNull(skill, "skill");
        return skillBase(skill.id());
    }

    /**
     * cia.job.<job>.skill.<sk>
     *
     * @param skillId expected format: job.skill
     */
    public static String skillBase(String skillId) {
        Objects.requireNonNull(skillId, "skillId");
        int dot = skillId.indexOf('.');
        if (dot <= 0 || dot == skillId.length() - 1) {
            throw new IllegalArgumentException("Invalid skill id (expected job.skill): " + skillId);
        }
        String job = skillId.substring(0, dot);
        String sk = skillId.substring(dot + 1);
        return "cia.job." + job + ".skill." + sk;
    }

    /**
     * 便捷：Skill 的 lore（默认 20 行）
     */
    public static List<Component> resolveSkillLore(SkillDefinition skill, Object... loreArgs) {
        Objects.requireNonNull(skill, "skill");
        return resolveLore(20, i -> skillLore(skill, i), loreArgs);
    }

    /**
     * 通用 lore 解析：
     * - 从 1..maxLines
     * - 遇到 I18n.has(key)==false 就停止
     * - 每行走 I18n.langNP(key, args)
     */
    public static List<Component> resolveLore(int maxLines, IntFunction<String> lineKey, Object... loreArgs) {
        Objects.requireNonNull(lineKey, "lineKey");
        int max = Math.max(1, maxLines);

        return IntStream.rangeClosed(1, max)
                .mapToObj(lineKey)
                .takeWhile(I18n::has)
                .map(key -> (loreArgs == null || loreArgs.length == 0)
                        ? I18n.langNP(key)
                        : I18n.langNP(key, loreArgs))
                .collect(Collectors.toList());
    }

    // ------------------------
    // Job keys
    // ------------------------

    /**
     * cia.job.<jobId>.name
     */
    public static String jobName(JobId jobId) {
        return jobBase(jobId) + ".name";
    }

    /**
     * cia.job.<jobId>
     */
    public static String jobBase(JobId jobId) {
        Objects.requireNonNull(jobId, "jobId");
        return "cia.job." + jobId;
    }

    /**
     * cia.job.<job>.skill.<sk>.lore.<line>
     */
    public static String skillLore(SkillDefinition skill, int line) {
        return skillBase(skill) + ".lore." + line;
    }

    /**
     * 便捷：Job 的 lore（默认 20 行）
     */
    public static List<Component> resolveJobLore(JobId jobId, Object... loreArgs) {
        Objects.requireNonNull(jobId, "jobId");
        return resolveLore(20, i -> jobLore(jobId, i), loreArgs);
    }

    // ------------------------
    // Generic lore resolver
    // ------------------------

    /**
     * cia.job.<jobId>.lore.<line>
     */
    public static String jobLore(JobId jobId, int line) {
        return jobBase(jobId) + ".lore." + line;
    }
}
