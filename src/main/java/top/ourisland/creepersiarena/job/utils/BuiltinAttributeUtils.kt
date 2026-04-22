package top.ourisland.creepersiarena.job.utils

import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.attribute.Attributable
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.EquipmentSlot
import java.util.*

/**
 * Registry-based attribute helper used by the built-in jobs.
 *
 * Modern Paper versions expose attributes through [Registry.ATTRIBUTE] and prefer key-based [AttributeModifier]
 * construction. This object wraps those APIs so the migrated job code can stay readable and consistent while avoiding
 * deprecated calls such as legacy enum-style `Attribute.valueOf(...)` lookups or UUID/name-based modifier
 * constructors.
 *
 * ## Accepted input forms
 * The resolver is intentionally forgiving because the codebase still contains a mix of naming styles inherited from the
 * datapack migration and older Bukkit idioms. The public lookup functions accept:
 * - legacy aliases such as `GENERIC_ATTACK_DAMAGE`
 * - plain modern paths such as `attack_damage`
 * - dotted forms such as `minecraft.attack_damage`
 * - fully namespaced keys such as `minecraft:attack_damage`
 *
 * All successful resolutions end up as registry-backed [Attribute] values.
 *
 * ## Failure behaviour
 * - [attribute] fails fast when nothing matches.
 * - [attributeOrNull] returns `null` when the running Paper version does not expose any of the requested aliases.
 * - [setBaseValue] and [baseValue] are safe convenience wrappers for entity attribute instances.
 *
 * @see Registry.ATTRIBUTE
 * @see BuiltinKeys
 */
object BuiltinAttributeUtils {

    private val aliasToPath: Map<String, String> = mapOf(
        "attack_damage" to "attack_damage",
        "generic_attack_damage" to "attack_damage",
        "attack_speed" to "attack_speed",
        "generic_attack_speed" to "attack_speed",
        "armor" to "armor",
        "generic_armor" to "armor",
        "knockback_resistance" to "knockback_resistance",
        "generic_knockback_resistance" to "knockback_resistance",
        "max_health" to "max_health",
        "generic_max_health" to "max_health",
        "movement_speed" to "movement_speed",
        "generic_movement_speed" to "movement_speed",
        "scale" to "scale",
        "generic_scale" to "scale"
    )

    /**
     * Resolves the first matching attribute from the supplied aliases.
     *
     * Use this when the attribute is required for correct behaviour and the calling code prefers a clear failure over a
     * silent no-op.
     *
     * @param names candidate aliases or namespaced keys to try in order
     * @return resolved registry-backed attribute
     * @throws IllegalStateException when none of the provided names can be resolved on the current server
     */
    @JvmStatic
    fun attribute(vararg names: String): Attribute =
        attributeOrNull(*names)
            ?: throw IllegalStateException("Missing attribute for aliases: "+ names.toList())

    /**
     * Resolves the first matching attribute from the supplied aliases.
     *
     * This variant is appropriate when the caller wants to gracefully adapt to Paper-version differences or optional
     * attributes such as newly introduced registry entries.
     *
     * @param names candidate aliases or namespaced keys to try in order
     * @return the resolved attribute, or `null` when none of the aliases exist on the running Paper version
     */
    @JvmStatic
    fun attributeOrNull(vararg names: String): Attribute? =
        names.asSequence()
            .mapNotNull(::normalizeAttributeKey)
            .firstNotNullOfOrNull(Registry.ATTRIBUTE::get)

    /**
     * Creates a modern key-based [AttributeModifier] for the supplied equipment slot.
     *
     * Modifier keys are namespaced through [BuiltinKeys] so built-in items do not collide with addon-defined modifiers.
     * The modifier is bound to the slot's group, matching current Paper expectations.
     *
     * @param path plugin-local path used to build the modifier key
     * @param amount modifier amount
     * @param operation arithmetic operation applied by the modifier
     * @param slot equipment slot whose group should receive the modifier
     * @return a key-based attribute modifier suitable for current Paper APIs
     */
    @JvmStatic
    fun modifier(
        path: String,
        amount: Double,
        operation: AttributeModifier.Operation,
        slot: EquipmentSlot
    ): AttributeModifier = AttributeModifier(
        BuiltinKeys.key(path),
        amount,
        operation,
        slot.group
    )

    /**
     * Reads the current base value of an attribute on an [Attributable].
     *
     * Both the attribute lookup and the entity's concrete attribute instance are optional on Paper, so this method
     * returns `null` when either step is unavailable.
     *
     * @param entity attribute owner
     * @param names candidate attribute aliases
     * @return current base value, or `null` when the attribute is unavailable for the entity
     */
    @JvmStatic
    fun baseValue(entity: Attributable, vararg names: String): Double? =
        attributeOrNull(*names)
            ?.let(entity::getAttribute)
            ?.baseValue

    /**
     * Sets the base value of an attribute on an [Attributable].
     *
     * This is a safe convenience wrapper used by the built-in mechanics listeners and skill logic. No exception is thrown
     * when the attribute or instance is unavailable; the method simply reports failure.
     *
     * @param entity attribute owner
     * @param value new base value to assign
     * @param names candidate attribute aliases
     * @return `true` when the attribute instance exists and was updated, otherwise `false`
     */
    @JvmStatic
    fun setBaseValue(entity: Attributable, value: Double, vararg names: String): Boolean {
        val attribute = attributeOrNull(*names) ?: return false
        val instance = entity.getAttribute(attribute) ?: return false
        instance.baseValue = value
        return true
    }

    /**
     * Normalizes a loose attribute name into a [NamespacedKey] suitable for registry lookup.
     *
     * Accepted inputs include legacy aliases, plain paths, dotted identifiers and full namespaced keys. The helper keeps
     * this logic private because callers should reason in terms of attribute aliases rather than key normalization rules.
     */
    private fun normalizeAttributeKey(raw: String?): NamespacedKey? {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isEmpty()) return null

        val lowered = trimmed.lowercase(Locale.ROOT)
        aliasToPath[lowered]?.let { return NamespacedKey.minecraft(it) }

        if (':' in lowered) {
            return NamespacedKey.fromString(lowered)
        }

        val normalizedPath = lowered
            .removePrefix("minecraft.")
            .removePrefix("generic.")
            .replace('.', '_')

        return NamespacedKey.minecraft(normalizedPath)
    }

}
