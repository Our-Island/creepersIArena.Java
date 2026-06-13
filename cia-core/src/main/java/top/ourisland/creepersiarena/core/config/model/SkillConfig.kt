package top.ourisland.creepersiarena.core.config.model

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import top.ourisland.creepersiarena.api.config.ISkillConfigView
import top.ourisland.creepersiarena.api.identity.CiaConfigPaths
import top.ourisland.creepersiarena.api.skill.SkillId

class SkillConfig private constructor(
    private val root: ConfigurationSection?,
) : ISkillConfigView {

    companion object {

        @JvmStatic
        fun defaults(): SkillConfig = SkillConfig(null)

        @JvmStatic
        fun fromYaml(yml: YamlConfiguration?): SkillConfig = SkillConfig(yml)
    }

    private fun sectionOf(skillId: SkillId?): ConfigurationSection? =
        skillId?.let { root?.getConfigurationSection(CiaConfigPaths.section(it)) }

    override fun cooldownSeconds(skillId: SkillId, def: Int): Int =
        getInt(skillId, "cooldown-seconds", def)

    override fun getInt(
        skillId: SkillId?,
        key: String?,
        def: Int
    ): Int {
        val value = value(skillId, key) ?: return def
        return when (value) {
            is Byte, is Short, is Int -> (value as Number).toInt()
            is Long -> value.takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()
                ?: invalid(skillId, key, "integer", value)

            else -> invalid(skillId, key, "integer", value)
        }
    }

    override fun getLong(
        skillId: SkillId?,
        key: String?,
        def: Long
    ): Long {
        val value = value(skillId, key) ?: return def
        return when (value) {
            is Byte, is Short, is Int, is Long -> (value as Number).toLong()
            else -> invalid(skillId, key, "integer", value)
        }
    }

    override fun getDouble(
        skillId: SkillId?,
        key: String?,
        def: Double
    ): Double {
        val value = value(skillId, key) ?: return def
        val number = value as? Number
            ?: invalid(skillId, key, "number", value)
        return number.toDouble().takeIf(Double::isFinite)
            ?: invalid(skillId, key, "finite number", value)
    }

    override fun getBoolean(
        skillId: SkillId?,
        key: String?,
        def: Boolean
    ): Boolean {
        val value = value(skillId, key) ?: return def
        return value as? Boolean
            ?: invalid(skillId, key, "boolean", value)
    }

    private fun value(skillId: SkillId?, key: String?): Any? {
        if (skillId == null || key == null) return null
        val section = sectionOf(skillId) ?: return null
        return section.get(key)
    }

    private fun <T> invalid(
        skillId: SkillId?,
        key: String?,
        expected: String,
        actual: Any
    ): T {
        val section = skillId?.let(CiaConfigPaths::section) ?: "<unknown>"
        throw IllegalArgumentException(
            "Invalid value at $section.$key: expected $expected, got ${actual::class.java.simpleName} ($actual)"
        )
    }

}
