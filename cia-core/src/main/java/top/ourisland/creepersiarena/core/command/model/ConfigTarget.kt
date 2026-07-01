package top.ourisland.creepersiarena.core.command.model

import java.util.*

/**
 * Target configuration file for /ciaa config.
 */
enum class ConfigTarget(
    private val rawId: String
) {

    CONFIG("config"),
    ARENA("arena"),
    SKILL("skill");

    fun id(): String = rawId

    fun fileName(): String = "$rawId.yml"

    companion object {

        @JvmStatic
        fun parse(raw: String?): ConfigTarget? {
            val normalized = raw?.trim()?.lowercase(Locale.ROOT) ?: return null
            return entries.firstOrNull { it.rawId == normalized }
        }

    }

}
