package top.ourisland.creepersiarena.core.config.model

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import top.ourisland.creepersiarena.api.config.ISkillConfigView
import top.ourisland.creepersiarena.api.identity.CiaKey
import top.ourisland.creepersiarena.api.identity.CiaNamespace
import top.ourisland.creepersiarena.api.skill.SkillId
import java.util.*

class SkillConfig private constructor(
    private val byId: Map<SkillId, ConfigurationSection>,
) : ISkillConfigView {

    companion object {

        @JvmStatic
        fun defaults(): SkillConfig = SkillConfig(Collections.emptyMap())

        @JvmStatic
        fun fromYaml(yml: YamlConfiguration?): SkillConfig {
            if (yml == null) return defaults()

            val map = HashMap<SkillId, ConfigurationSection>()
            for (rootKey in yml.getKeys(false)) {
                val root = yml.getConfigurationSection(rootKey) ?: continue
                collectSections(CiaNamespace.parse(rootKey), root, "", map)
            }
            return SkillConfig(map)
        }

        private fun collectSections(
            namespace: CiaNamespace,
            sec: ConfigurationSection,
            path: String,
            out: MutableMap<SkillId, ConfigurationSection>
        ) {
            for (key in sec.getKeys(false)) {
                val child = sec.getConfigurationSection(key) ?: continue
                val nextPath = if (path.isEmpty()) key else "$path/$key"
                val id = try {
                    SkillId.of(CiaKey.of(namespace, nextPath))
                } catch (exception: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Invalid skill config path ${namespace.value()}:$nextPath",
                        exception
                    )
                }
                out.putIfAbsent(id, child)
                collectSections(namespace, child, nextPath, out)
            }
        }

    }

    private fun sectionOf(skillId: SkillId?): ConfigurationSection? =
        skillId?.let(byId::get)

    override fun cooldownSeconds(skillId: SkillId, def: Int): Int =
        getInt(skillId, "cooldown-seconds", def)

    override fun getInt(
        skillId: SkillId?,
        key: String?,
        def: Int
    ): Int {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getInt(key, def)
        } catch (_: Throwable) {
            def
        }
    }

    override fun getLong(
        skillId: SkillId?,
        key: String?,
        def: Long
    ): Long {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getLong(key, def)
        } catch (_: Throwable) {
            def
        }
    }

    override fun getDouble(
        skillId: SkillId?,
        key: String?,
        def: Double
    ): Double {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getDouble(key, def)
        } catch (_: Throwable) {
            def
        }
    }

    override fun getBoolean(
        skillId: SkillId?,
        key: String?,
        def: Boolean
    ): Boolean {
        val sec = sectionOf(skillId)
        if (sec == null || key == null) return def
        return try {
            sec.getBoolean(key, def)
        } catch (_: Throwable) {
            def
        }
    }

}
