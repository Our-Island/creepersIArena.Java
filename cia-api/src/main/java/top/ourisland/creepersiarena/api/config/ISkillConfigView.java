package top.ourisland.creepersiarena.api.config;

import top.ourisland.creepersiarena.api.skill.SkillId;

/**
 * Stable read-only view of skill configuration exposed to skill extensions.
 */
public interface ISkillConfigView {

    int cooldownSeconds(
            SkillId skillId,
            int defaultValue
    );

    int getInt(
            SkillId skillId,
            String key,
            int defaultValue
    );

    long getLong(
            SkillId skillId,
            String key,
            long defaultValue
    );

    double getDouble(
            SkillId skillId,
            String key,
            double defaultValue
    );

    boolean getBoolean(
            SkillId skillId,
            String key,
            boolean defaultValue
    );

}
