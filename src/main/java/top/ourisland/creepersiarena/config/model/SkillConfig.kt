package top.ourisland.creepersiarena.config.model

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import org.jspecify.annotations.Nullable
import java.util.Collections
import kotlin.jvm.JvmStatic

/**
 * Skill-specific configuration loaded from skill.yml.
 *
 * Expected structure (recommended):
 *
 * ```yml
 * cia:
 *   creeper:
 *     fireworks:
 *       cooldown-seconds: 20
 *       forward: 1.0
 * ```
 *
 * Skill id is the `ISkillDefinition#id()` value (e.g. "creeper.fireworks").
 */
class SkillConfig private constructor(
    private val byId: Map<String, ConfigurationSection>,
) {
    companion object {
        @JvmStatic
        fun defaults(): SkillConfig = SkillConfig(Collections.emptyMap())

        @JvmStatic
        fun fromYaml(yml: @Nullable YamlConfiguration?): SkillConfig {
            if (yml == null) return defaults()

            val root = yml.getConfigurationSection("cia") ?: return defaults()
            val map = HashMap<String, ConfigurationSection>()

            collectSections(root, "", map)

            try {
                for ((k, v) in root.getValues(false)) {
                    if (k != null && k.contains('.')) {
                        when (v) {
                            is ConfigurationSection -> map.putIfAbsent(k, v)
                            is MemorySection -> map.putIfAbsent(k, v)
                        }
                    }
                }
            } catch (_: Throwable) {
            }

            return SkillConfig(map)
        }

        private fun collectSections(sec: ConfigurationSection, prefix: String, out: MutableMap<String, ConfigurationSection>) {
            for (key in sec.getKeys(false)) {
                val child = sec.getConfigurationSection(key) ?: continue
                val id = if (prefix.isEmpty()) key else "$prefix.$key"
                out.putIfAbsent(id, child)
                collectSections(child, id, out)
            }
        }
    }

    private fun sectionOf(skillId: String?): ConfigurationSection? {
        if (skillId.isNullOrEmpty()) return null
        return byId[skillId]
    }

    fun cooldownSeconds(skillId: String, def: Int): Int = getInt(skillId, "cooldown-seconds", def)

    fun getInt(skillId: String?, key: String?, def: Int): Int {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getInt(key, def)
        } catch (_: Throwable) {
            def
        }
    }

    fun getLong(skillId: String?, key: String?, def: Long): Long {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getLong(key, def)
        } catch (_: Throwable) {
            def
        }
    }

    fun getDouble(skillId: String?, key: String?, def: Double): Double {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getDouble(key, def)
        } catch (_: Throwable) {
            def
        }
    }

    fun getBoolean(skillId: String?, key: String?, def: Boolean): Boolean {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getBoolean(key, def)
        } catch (_: Throwable) {
            def
        }
    }

}
