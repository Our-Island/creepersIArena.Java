package top.ourisland.creepersiarena.utils

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.utils.Msg.title
import top.ourisland.creepersiarena.utils.Msg.titleMini
import java.time.Duration

/**
 * Adventure messaging helper used across the plugin.
 *
 * ## Why this exists
 * - Paper uses the Adventure API natively. Most outputs should be sent as [Component]s.
 * - On Paper, [CommandSender] implements [Audience], so you can safely call Adventure methods on any sender
 *   (players, console, command blocks, etc.).
 *
 * ## MiniMessage
 * MiniMessage strings (e.g. `"<red>Hello</red>"`) are parsed via [MiniMessage] and sent as Components.
 *
 * ## Null handling (title/subtitle)
 * For convenience, [title] / [titleMini] treat `null` title or subtitle as empty (`""` / [Component.empty]).
 *
 * @see Audience
 * @see MiniMessage
 * @see Title
 */
object Msg {

    private val MINI: MiniMessage = MiniMessage.miniMessage()

    /* =========================
     * Basic: Component / plain text
     * ========================= */

    /** Sends a component message to a command sender. */
    @JvmStatic
    fun send(sender: CommandSender, message: Component) =
        sender.sendMessage(message)

    /** Sends a plain-text message (no formatting) to a command sender. */
    @JvmStatic
    fun send(sender: CommandSender, plainText: String) =
        sender.sendMessage(Component.text(plainText))

    /** Sends a component action bar to a command sender. */
    @JvmStatic
    fun actionBar(sender: CommandSender, message: Component) =
        sender.sendActionBar(message)

    /** Sends a plain-text action bar (no formatting) to a command sender. */
    @JvmStatic
    fun actionBar(sender: CommandSender, plainText: String) =
        sender.sendActionBar(Component.text(plainText))

    /* =========================
     * MiniMessage
     * ========================= */

    /** Parses [miniMessage] with MiniMessage and sends it as a normal chat message. */
    @JvmStatic
    fun sendMini(sender: CommandSender, miniMessage: String) =
        sender.sendMessage(mini(miniMessage))

    /** Parses [miniMessage] with MiniMessage and sends it as an action bar. */
    @JvmStatic
    fun actionBarMini(sender: CommandSender, miniMessage: String) =
        sender.sendActionBar(mini(miniMessage))

    /* =========================
     * Title / Subtitle
     * ========================= */

    /**
     * Shows a title/subtitle to the sender.
     *
     * @param title title component; `null` is treated as empty.
     * @param subtitle subtitle component; `null` is treated as empty.
     */
    @JvmStatic
    fun title(sender: CommandSender, title: Component?, subtitle: Component?) =
        sender.showTitle(titleOf(title, subtitle))

    /**
     * Shows a title/subtitle with custom timings.
     *
     * @param fadeIn time to fade in
     * @param stay time to stay
     * @param fadeOut time to fade out
     */
    @JvmStatic
    fun title(
        sender: CommandSender,
        title: Component?,
        subtitle: Component?,
        fadeIn: Duration,
        stay: Duration,
        fadeOut: Duration
    ) {
        sender.showTitle(titleOf(title, subtitle, fadeIn, stay, fadeOut))
    }

    /**
     * Parses MiniMessage strings and shows them as a title/subtitle.
     *
     * `null` title/subtitle are treated as empty strings.
     */
    @JvmStatic
    fun titleMini(sender: CommandSender, titleMini: String?, subtitleMini: String?) =
        sender.showTitle(titleOf(mini(titleMini), mini(subtitleMini)))

    /**
     * Parses MiniMessage strings and shows them as a title/subtitle with custom timings.
     *
     * `null` title/subtitle are treated as empty strings.
     */
    @JvmStatic
    fun titleMini(
        sender: CommandSender,
        titleMini: String?,
        subtitleMini: String?,
        fadeIn: Duration,
        stay: Duration,
        fadeOut: Duration
    ) {
        sender.showTitle(titleOf(mini(titleMini), mini(subtitleMini), fadeIn, stay, fadeOut))
    }

    /* =========================
     * Audience overloads
     * ========================= */

    /** Sends a component message to an [Audience]. */
    @JvmStatic
    fun send(audience: Audience, message: Component) =
        audience.sendMessage(message)

    /** Parses [miniMessage] and sends it to an [Audience]. */
    @JvmStatic
    fun sendMini(audience: Audience, miniMessage: String) =
        audience.sendMessage(mini(miniMessage))

    /** Sends a component action bar to an [Audience]. */
    @JvmStatic
    fun actionBar(audience: Audience, message: Component) =
        audience.sendActionBar(message)

    /** Parses [miniMessage] and sends it as an action bar to an [Audience]. */
    @JvmStatic
    fun actionBarMini(audience: Audience, miniMessage: String) =
        audience.sendActionBar(mini(miniMessage))

    /** Shows a title/subtitle to an [Audience]. `null` becomes empty. */
    @JvmStatic
    fun title(audience: Audience, title: Component?, subtitle: Component?) =
        audience.showTitle(titleOf(title, subtitle))

    /** Parses MiniMessage strings and shows them as a title/subtitle to an [Audience]. */
    @JvmStatic
    fun titleMini(audience: Audience, titleMini: String?, subtitleMini: String?) =
        audience.showTitle(titleOf(mini(titleMini), mini(subtitleMini)))

    /* =========================
     * Private helpers
     * ========================= */

    private fun mini(input: String?): Component =
        MINI.deserialize(input.orEmpty())

    private fun titleOf(title: Component?, subtitle: Component?): Title =
        Title.title(title ?: Component.empty(), subtitle ?: Component.empty())

    private fun titleOf(
        title: Component?,
        subtitle: Component?,
        fadeIn: Duration,
        stay: Duration,
        fadeOut: Duration
    ): Title = Title.title(
        title ?: Component.empty(),
        subtitle ?: Component.empty(),
        Title.Times.times(fadeIn, stay, fadeOut)
    )

}
