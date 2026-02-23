package top.ourisland.creepersiarena.config.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Skill-specific configuration loaded from skill.yml.
 *
 * <p>Expected structure (recommended):</p>
 * <pre>
 * skills:
 *   creeper:
 *     fireworks:
 *       cooldown-seconds: 20
 *       forward: 1.0
 * </pre>
 *
 * <p>Skill id is the {@link top.ourisland.creepersiarena.job.skill.ISkillDefinition#id()} value
 * (e.g. "creeper.fireworks").</p>
 */
public final class SkillConfig {

    private final Map<String, ConfigurationSection> byId;

    private SkillConfig(Map<String, ConfigurationSection> byId) {
        this.byId = byId;
    }

    public static SkillConfig defaults() {
        return new SkillConfig(Collections.emptyMap());
    }

    public static SkillConfig fromYaml(@Nullable YamlConfiguration yml) {
        if (yml == null) return defaults();

        ConfigurationSection root = yml.getConfigurationSection("cia");
        if (root == null) return defaults();

        Map<String, ConfigurationSection> map = new HashMap<>();

        collectSections(root, "", map);

        try {
            for (var e : root.getValues(false).entrySet()) {
                String k = e.getKey();
                Object v = e.getValue();
                if (k != null && k.contains(".") && v instanceof ConfigurationSection sec) {
                    map.putIfAbsent(k, sec);
                } else if (k != null && k.contains(".") && v instanceof MemorySection ms) {
                    map.putIfAbsent(k, ms);
                }
            }
        } catch (Throwable ignored) {
        }

        return new SkillConfig(map);
    }

    private static void collectSections(ConfigurationSection sec, String prefix, Map<String, ConfigurationSection> out) {
        for (String key : sec.getKeys(false)) {
            if (key == null) continue;

            ConfigurationSection child = sec.getConfigurationSection(key);
            if (child == null) continue;

            String id = prefix.isEmpty() ? key : (prefix + "." + key);

            out.putIfAbsent(id, child);

            collectSections(child, id, out);
        }
    }

    private @Nullable ConfigurationSection sectionOf(String skillId) {
        if (skillId == null || skillId.isEmpty()) return null;

        return byId.get(skillId);
    }

    public int cooldownSeconds(String skillId, int def) {
        return getInt(skillId, "cooldown-seconds", def);
    }

    public int getInt(String skillId, String key, int def) {
        ConfigurationSection sec = sectionOf(skillId);
        if (sec == null || key == null) return def;
        try {
            return sec.getInt(key, def);
        } catch (Throwable ignored) {
            return def;
        }
    }

    public long getLong(String skillId, String key, long def) {
        ConfigurationSection sec = sectionOf(skillId);
        if (sec == null || key == null) return def;
        try {
            return sec.getLong(key, def);
        } catch (Throwable ignored) {
            return def;
        }
    }

    public double getDouble(String skillId, String key, double def) {
        ConfigurationSection sec = sectionOf(skillId);
        if (sec == null || key == null) return def;
        try {
            return sec.getDouble(key, def);
        } catch (Throwable ignored) {
            return def;
        }
    }

    public boolean getBoolean(String skillId, String key, boolean def) {
        ConfigurationSection sec = sectionOf(skillId);
        if (sec == null || key == null) return def;
        try {
            return sec.getBoolean(key, def);
        } catch (Throwable ignored) {
            return def;
        }
    }

}
