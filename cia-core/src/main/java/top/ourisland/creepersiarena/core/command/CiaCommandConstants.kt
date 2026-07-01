package top.ourisland.creepersiarena.core.command

/**
 * Shared command literals and small static suggestion sets.
 */
object CiaCommandConstants {

    const val PLAYER_ROOT_LITERAL: String = "cia"
    const val ADMIN_ROOT_LITERAL: String = "ciaa"

    @JvmField
    val PLAYER_ROOT_ALIASES: List<String> = listOf()

    @JvmField
    val ADMIN_ROOT_ALIASES: List<String> = listOf()

    @JvmField
    val PLAYER_LANGUAGE_SUGGESTIONS: List<String> = listOf("default", "en_us", "zh_cn")

    @JvmField
    val ADMIN_LANGUAGE_SUGGESTIONS: List<String> = listOf("en_us", "zh_cn")

    @JvmField
    val CONFIG_TARGET_SUGGESTIONS: List<String> = listOf("config", "arena", "skill")

    @JvmField
    val BOOLEAN_SUGGESTIONS: List<String> = listOf("true", "false")

    @JvmField
    val PREFERENCE_BOOLEAN_SUGGESTIONS: List<String> = listOf("on", "off", "true", "false")

    @JvmField
    val TEAM_SUGGESTIONS: List<String> = listOf(
        "random",
        "1", "2", "3", "4",
        "red", "blue", "green", "yellow",
        "aqua", "cyan", "purple", "white", "black"
    )

}
