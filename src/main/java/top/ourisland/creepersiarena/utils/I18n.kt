package top.ourisland.creepersiarena.utils

import net.kyori.adventure.text.Component
import org.slf4j.Logger
import top.ourisland.creepersiarena.config.ConfigManager
import top.ourisland.creepersiarena.utils.I18n.has
import top.ourisland.creepersiarena.utils.I18n.init
import top.ourisland.creepersiarena.utils.I18n.reload
import java.text.MessageFormat
import java.util.*

/**
 * Plugin i18n helper backed by Java [ResourceBundle].
 *
 * ## Resource layout
 * Bundles are loaded from plugin resources using the base name `lang/<lang>`. For example, if global
 * config returns `en_us`, it loads `lang/en_us`, which typically corresponds to
 * `src/main/resources/lang/en_us.properties`.
 *
 * ## Loading and fallback
 * - [init] loads the bundle immediately according to `globalConfig().lang()`.
 * - [reload] re-reads `globalConfig().lang()` and reloads the bundle.
 * - If loading the configured language fails, it falls back to `lang/en_us`.
 *
 * ## Prefix vs No Prefix (NP)
 * The common prefix is stored under:
 *
 * ```text
 * cia.prefix
 * ```
 *
 * - `lang*` methods **include** prefix: `prefix + pattern/format`.
 * - `lang*NP` methods are **No Prefix**: only `pattern/format`.
 *
 * ## Formatting (MessageFormat)
 * Formatting uses [MessageFormat] with placeholders:
 *
 * ```text
 * {0} {1} {2} ...
 * ```
 *
 * Example:
 * ```text
 * key = "Hello {0}, score={1}"
 * I18n.langStrNP(key, "Angela", 42)
 * => "Hello Angela, score=42"
 * ```
 *
 * Notes:
 * - A literal single quote `'` must be escaped as `''` in MessageFormat patterns.
 *
 * ## Initialization contract
 * You must call [init] once during plugin startup before using other methods.
 * Calling [reload] before [init] throws [IllegalStateException] (close to the original Java behavior).
 *
 * ## Missing keys
 * - Use [has] to check key existence.
 * - Calling `lang*` / `lang*NP` with a missing key throws [MissingResourceException].
 */
object I18n {

    private var bundle: ResourceBundle? = null
    private var logger: Logger? = null
    private var configManager: ConfigManager? = null

    /**
     * Initializes i18n and loads the bundle based on `globalConfig().lang()`.
     *
     * Typical usage:
     * ```text
     * I18n.init(configManager, logger)
     * ```
     *
     * @param configManager provider of `globalConfig().lang()`
     * @param logger logger used to warn when a language fails to load and fallback occurs
     */
    @JvmStatic
    fun init(configManager: ConfigManager, logger: Logger) {
        this.configManager = configManager
        this.logger = logger
        reload()
    }

    /**
     * Reloads the active language bundle based on `globalConfig().lang()`.
     *
     * It tries `lang/<configuredLang>` and falls back to `lang/en_us`.
     *
     * @throws IllegalStateException if called before [init]
     */
    @JvmStatic
    fun reload() {
        loadBundle(configManager!!.globalConfig.lang)
    }

    private fun loadBundle(lang: String) {
        bundle = try {
            ResourceBundle.getBundle("lang/$lang")
        } catch (_: Exception) {
            logger?.warn("[I18n] Failed to load language '{}', fallback to en_us", lang)
            ResourceBundle.getBundle("lang/en_us")
        }
    }

    /* =========================
     * With prefix
     * ========================= */

    /**
     * Resolves [key] to a [Component] **with prefix**.
     *
     * Equivalent to:
     * ```text
     * Component.text(I18n.langStr(key))
     * ```
     *
     * @param key language key
     * @return resolved message as a component (prefix included)
     * @throws MissingResourceException if `cia.prefix` or [key] is missing in the bundle
     */
    @JvmStatic
    fun lang(key: String): Component = Component.text(langStr(key))

    /**
     * Resolves and formats [key] to a [Component] **with prefix**.
     *
     * Formatting uses MessageFormat placeholders:
     * ```text
     * {0} {1} {2} ...
     * ```
     *
     * @param key language key
     * @param args formatting arguments (optional)
     * @return resolved message as a component (prefix included)
     * @throws MissingResourceException if `cia.prefix` or [key] is missing in the bundle
     */
    @JvmStatic
    fun lang(key: String, vararg args: Any?): Component = Component.text(langStr(key, *args))

    /**
     * Resolves [key] to a String **with prefix**.
     *
     * Output:
     * ```text
     * <cia.prefix> + <pattern(key)>
     * ```
     *
     * @param key language key
     * @return resolved string (prefix included)
     * @throws MissingResourceException if `cia.prefix` or [key] is missing in the bundle
     */
    @JvmStatic
    fun langStr(key: String): String = prefixStr() + pattern(key)

    /**
     * Resolves and formats [key] to a String **with prefix**.
     *
     * Output:
     * ```text
     * <cia.prefix> + MessageFormat.format(pattern(key), args...)
     * ```
     *
     * If [args] is empty, no formatting is performed.
     *
     * @param key language key
     * @param args formatting arguments (optional)
     * @return resolved string (prefix included)
     * @throws MissingResourceException if `cia.prefix` or [key] is missing in the bundle
     */
    @JvmStatic
    fun langStr(key: String, vararg args: Any?): String = prefixStr() + format(key, *args)

    /* =========================
     * No prefix (NP)
     * ========================= */

    /**
     * Resolves [key] to a [Component] **without prefix** (NP = No Prefix).
     *
     * @param key language key
     * @return resolved message as a component (no prefix)
     * @throws MissingResourceException if [key] is missing in the bundle
     */
    @JvmStatic
    fun langNP(key: String): Component = Component.text(langStrNP(key))

    /**
     * Resolves and formats [key] to a [Component] **without prefix** (NP = No Prefix).
     *
     * @param key language key
     * @param args formatting arguments (optional)
     * @return resolved message as a component (no prefix)
     * @throws MissingResourceException if [key] is missing in the bundle
     */
    @JvmStatic
    fun langNP(key: String, vararg args: Any?): Component = Component.text(langStrNP(key, *args))

    /**
     * Resolves [key] to a String **without prefix** (NP = No Prefix).
     *
     * @param key language key
     * @return resolved string (no prefix)
     * @throws MissingResourceException if [key] is missing in the bundle
     */
    @JvmStatic
    fun langStrNP(key: String): String = pattern(key)

    /**
     * Resolves and formats [key] to a String **without prefix** (NP = No Prefix).
     *
     * @param key language key
     * @param args formatting arguments (optional)
     * @return resolved string (no prefix)
     * @throws MissingResourceException if [key] is missing in the bundle
     */
    @JvmStatic
    fun langStrNP(key: String, vararg args: Any?): String = format(key, *args)

    /**
     * Resolves [key] to a [Component] **without prefix** (NP), or returns `null` if [key] is missing.
     *
     * Useful for optional lines:
     * ```text
     * val c = I18n.langOrNullNP("optional.key")
     * if (c != null) sender.sendMessage(c)
     * ```
     *
     * @param key language key
     * @param args formatting arguments (optional)
     * @return resolved component (no prefix), or `null` if key does not exist
     */
    @JvmStatic
    fun langOrNullNP(key: String, vararg args: Any?): Component? {
        if (!has(key)) return null
        return if (args.isEmpty()) langNP(key) else langNP(key, *args)
    }

    /**
     * Checks whether the current bundle contains [key].
     *
     * If the bundle is not loaded yet (i.e. [init] not called or bundle failed), this returns `false`.
     *
     * @param key language key
     * @return `true` if the bundle exists and contains the key
     */
    @JvmStatic
    fun has(key: String): Boolean = bundle?.containsKey(key) == true

    /* =========================
     * Internals
     * ========================= */

    /**
     * Reads the prefix string from `cia.prefix`.
     *
     * - If bundle is not loaded, returns empty string (mirrors original Java behavior when bundle is null).
     *
     * @return prefix string or empty if bundle is null
     * @throws MissingResourceException if bundle is loaded but `cia.prefix` is missing
     */
    private fun prefixStr(): String {
        val b = bundle ?: return ""
        return b.getString("cia.prefix")
    }

    /**
     * Reads the raw pattern for [key].
     *
     * - If bundle is not loaded, returns [key] (mirrors original Java behavior when bundle is null).
     *
     * @param key language key
     * @return raw pattern string (or key itself if bundle is null)
     * @throws MissingResourceException if bundle is loaded but [key] is missing
     */
    private fun pattern(key: String): String {
        val b = bundle ?: return key
        return b.getString(key)
    }

    /**
     * Formats [key] using [MessageFormat] if [args] is not empty; otherwise returns the raw pattern.
     *
     * @param key language key
     * @param args formatting arguments (optional)
     * @return formatted string or raw pattern when args is empty
     * @throws MissingResourceException if bundle is loaded but [key] is missing
     */
    private fun format(key: String, vararg args: Any?): String {
        val p = pattern(key)
        return if (args.isEmpty()) p else MessageFormat.format(p, *args)
    }
}
