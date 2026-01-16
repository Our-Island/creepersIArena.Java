package top.ourisland.creepersiarena.util;

import net.kyori.adventure.text.Component;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.skill.Skill;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class LangKeyResolver {
    private LangKeyResolver() {
    }

    /**
     * cia.job.<job>.skill.<sk>.name
     */
    public static String skillName(Skill skill) {
        return skillBase(skill) + ".name";
    }

    /**
     * cia.job.<job>.skill.<sk>
     */
    public static String skillBase(Skill skill) {
        Objects.requireNonNull(skill, "skill");
        String id = skill.id();
        int dot = id.indexOf('.');
        if (dot <= 0 || dot == id.length() - 1) {
            throw new IllegalArgumentException("Invalid skill id (expected job.skill): " + id);
        }
        String job = id.substring(0, dot);
        String sk = id.substring(dot + 1);
        return "cia.job." + job + ".skill." + sk;
    }

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
     * 便捷：Skill 的 lore（默认 20 行）
     */
    public static List<Component> resolveSkillLore(Skill skill, Object... loreArgs) {
        Objects.requireNonNull(skill, "skill");
        return resolveLore(20, i -> skillLore(skill, i), loreArgs);
    }

    /**
     * 通用 lore 解析： - 从 1..maxLines - 遇到 I18n.has(key)==false 就停止 - 每行走 I18n.langNP(key, args)
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

    /**
     * cia.job.<job>.skill.<sk>.lore.<line>
     */
    public static String skillLore(Skill skill, int line) {
        return skillBase(skill) + ".lore." + line;
    }

    /**
     * 便捷：Job 的 lore（默认 20 行）
     */
    public static List<Component> resolveJobLore(JobId jobId, Object... loreArgs) {
        Objects.requireNonNull(jobId, "jobId");
        return resolveLore(20, i -> jobLore(jobId, i), loreArgs);
    }

    /**
     * cia.job.<jobId>.lore.<line>
     */
    public static String jobLore(JobId jobId, int line) {
        return jobBase(jobId) + ".lore." + line;
    }
}
