package top.ourisland.creepersiarena.config.model

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import top.ourisland.creepersiarena.api.config.ISkillConfigView
import java.util.*

class SkillConfig private constructor(
    private val byId: Map<String, ConfigurationSection>,
) : ISkillConfigView {

    companion object {

        @JvmStatic
        fun defaults(): SkillConfig = SkillConfig(Collections.emptyMap())

        @JvmStatic
        fun fromYaml(yml: YamlConfiguration?): SkillConfig {
            if (yml == null) return defaults()

            val map = HashMap<String, ConfigurationSection>()
            for (rootKey in yml.getKeys(false)) {
                val root = yml.getConfigurationSection(rootKey) ?: continue
                collectSections(rootKey.lowercase(), root, "", map)
            }
            return SkillConfig(map)
        }

        private fun collectSections(
            namespace: String,
            sec: ConfigurationSection,
            path: String,
            out: MutableMap<String, ConfigurationSection>
        ) {
            for (key in sec.getKeys(false)) {
                val child = sec.getConfigurationSection(key) ?: continue
                val nextPath = if (path.isEmpty()) key.lowercase() else "$path.${key.lowercase()}"
                out.putIfAbsent("$namespace:$nextPath", child)
                if (namespace == "cia") {
                    out.putIfAbsent(nextPath, child)
                }
                collectSections(namespace, child, nextPath, out)
            }
        }

    }

    private fun sectionOf(skillId: String?): ConfigurationSection? {
        if (skillId.isNullOrEmpty()) return null
        val normalized = skillId.trim().lowercase()
        return byId[normalized]
            ?: byId[normalized.substringAfter(':', normalized)]
    }

    override fun cooldownSeconds(skillId: String, def: Int): Int = getInt(skillId, "cooldown-seconds", def)

    override fun getInt(skillId: String?, key: String?, def: Int): Int {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getInt(key, def)
        } catch (_: Throwable) {
            def
        }
    }

    override fun getLong(skillId: String?, key: String?, def: Long): Long {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getLong(key, def)
        } catch (_: Throwable) {
            def
        }
    }

    override fun getDouble(skillId: String?, key: String?, def: Double): Double {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getDouble(key, def)
        } catch (_: Throwable) {
            def
        }
    }

    override fun getBoolean(skillId: String?, key: String?, def: Boolean): Boolean {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getBoolean(key, def)
        } catch (_: Throwable) {
            def
        }
    }

}
