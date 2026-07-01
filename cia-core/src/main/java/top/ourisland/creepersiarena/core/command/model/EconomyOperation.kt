package top.ourisland.creepersiarena.core.command.model

/**
 * Mutating wallet operation used by /ciaa economy.
 */
enum class EconomyOperation(
    private val rawId: String
) {

    GIVE("give"),
    TAKE("take"),
    SET("set");

    fun id(): String = rawId

}
