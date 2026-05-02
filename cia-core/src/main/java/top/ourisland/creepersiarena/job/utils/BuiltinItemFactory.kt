package top.ourisland.creepersiarena.job.utils

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.trim.ArmorTrim
import org.bukkit.inventory.meta.trim.TrimMaterial
import org.bukkit.inventory.meta.trim.TrimPattern
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory.armor

/**
 * Central factory for the built-in jobs' display items, weapons and armor pieces.
 *
 * During the datapack-to-Paper migration, item creation became a repeated concern: every job needs translated-looking
 * names, hidden tooltip flags, modern attribute modifiers and sometimes armor trims or leather tint. This object keeps
 * those conventions in one place so job definitions can describe <em>what</em> an item should feel like without
 * re-implementing the Paper item-meta details each time.
 *
 * ## Scope
 * The factory is intentionally biased toward built-in content:
 * - weapon helpers attach standard attack-damage and attack-speed modifiers
 * - armor helpers support trim, leather colour, enchantments and arbitrary extra modifiers
 * - display helpers return hidden, UI-friendly item stacks suitable for hotbars and lobby previews
 *
 * @see BuiltinAttributeUtils
 */
object BuiltinItemFactory {

    /**
     * Builds a built-in weapon item.
     *
     * The returned item is marked unbreakable, receives hidden tooltip flags, and is given standard hand-slot attack
     * damage / attack speed modifiers through modern key-based [AttributeModifier]s. Optional enchantments are applied
     * afterwards.
     *
     * @param material weapon material
     * @param name plain-text display name
     * @param lore plain-text lore lines
     * @param attackDamageAmount additive attack damage modifier applied in hand
     * @param attackSpeedAmount additive attack speed modifier applied in hand
     * @param enchants optional enchantments to apply, or `null` for none
     * @return fully prepared weapon item
     */
    @JvmStatic
    fun weapon(
        material: Material,
        name: String,
        lore: List<String>,
        attackDamageAmount: Double,
        attackSpeedAmount: Double,
        enchants: Map<Enchantment, Int>?
    ): ItemStack {
        val item = skillItem(material, name, lore)
        val meta = item.itemMeta
        meta.isUnbreakable = true
        addModifier(
            meta,
            BuiltinAttributeUtils.attribute("attack_damage", "generic_attack_damage"),
            attackDamageAmount,
            AttributeModifier.Operation.ADD_NUMBER,
            EquipmentSlot.HAND,
            "weapon_damage"
        )
        addModifier(
            meta,
            BuiltinAttributeUtils.attribute("attack_speed", "generic_attack_speed"),
            attackSpeedAmount,
            AttributeModifier.Operation.ADD_NUMBER,
            EquipmentSlot.HAND,
            "weapon_speed"
        )
        enchants?.forEach { (enchant, level) -> meta.addEnchant(enchant, level, true) }
        hide(meta)
        item.itemMeta = meta
        return item
    }

    /**
     * Builds a simple hidden display item.
     *
     * This helper is used for skill icons and other UI-facing items that do not require custom attribute modifiers. Name
     * and lore are stored as plain-text [Component]s to match the rest of the Adventure-based codebase.
     *
     * @param material item material
     * @param name plain-text display name
     * @param lore plain-text lore lines
     * @return hidden display item
     */
    @JvmStatic
    fun skillItem(material: Material, name: String, lore: List<String>): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(Component.text(name))
        meta.lore(lore.map(Component::text))
        hide(meta)
        item.itemMeta = meta
        return item
    }

    /**
     * Adds a single attribute modifier to [meta].
     *
     * The helper is null-safe and silently returns when either [meta] or [attribute] is absent, which makes it suitable
     * for item templates that should gracefully degrade on older or feature-limited environments.
     *
     * @param meta item meta to mutate
     * @param attribute target attribute, or `null` to do nothing
     * @param amount modifier amount
     * @param operation arithmetic operation
     * @param slot equipment slot that should receive the modifier
     * @param name local modifier path; blank values fall back to `modifier`
     */
    @JvmStatic
    fun addModifier(
        meta: ItemMeta?,
        attribute: Attribute?,
        amount: Double,
        operation: AttributeModifier.Operation,
        slot: EquipmentSlot,
        name: String?
    ) {
        if (meta == null || attribute == null) return
        val path = name?.trim().takeUnless { it.isNullOrEmpty() } ?: "modifier"
        meta.addAttributeModifier(attribute, BuiltinAttributeUtils.modifier(path, amount, operation, slot))
    }

    /**
     * Applies all standard [ItemFlag] values to [meta].
     *
     * Built-in items intentionally hide their raw NBT-style tooltip details so players mainly see the curated name/lore
     * exposed by the plugin rather than Minecraft's default attribute lines.
     *
     * @param meta item meta to update; ignored when `null`
     */
    @JvmStatic
    fun hide(meta: ItemMeta?) {
        if (meta == null) return
        meta.addItemFlags(*ItemFlag.entries.toTypedArray())
    }

    /**
     * Builds an armor piece with built-in presentation and optional gameplay modifiers.
     *
     * The returned item is marked unbreakable, receives hidden tooltip flags, and may optionally include:
     * - an [ArmorTrim] when both [trimMaterial] and [trimPattern] are present and the meta supports armor trims
     * - a leather tint when [leatherColor] is present and the meta is [LeatherArmorMeta]
     * - arbitrary attribute modifiers described by [modifiers]
     * - enchantments described by [enchants]
     *
     * @param material armor material
     * @param name plain-text display name
     * @param lore plain-text lore lines
     * @param trimMaterial optional trim material
     * @param trimPattern optional trim pattern
     * @param leatherColor optional RGB colour for leather armor
     * @param modifiers optional extra attribute modifiers
     * @param enchants optional enchantments to apply
     * @return fully prepared armor item
     */
    @JvmStatic
    fun armor(
        material: Material,
        name: String,
        lore: List<String>,
        trimMaterial: TrimMaterial?,
        trimPattern: TrimPattern?,
        leatherColor: Int?,
        modifiers: List<ModifierSpec>?,
        enchants: Map<Enchantment, Int>?
    ): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(Component.text(name))
        meta.lore(lore.map(Component::text))
        meta.isUnbreakable = true

        if (trimMaterial != null && trimPattern != null && meta is ArmorMeta) {
            meta.trim = ArmorTrim(trimMaterial, trimPattern)
        }
        if (leatherColor != null && meta is LeatherArmorMeta) {
            meta.setColor(Color.fromRGB(leatherColor))
        }
        modifiers?.forEach { spec ->
            addModifier(meta, spec.attribute, spec.amount, spec.operation, spec.slot, spec.name)
        }
        enchants?.forEach { (enchant, level) ->
            meta.addEnchant(enchant, level, true)
        }
        hide(meta)
        item.itemMeta = meta
        return item
    }

    /**
     * Builds a [ModifierSpec] from one or more attribute aliases.
     *
     * This overload is useful when call sites want to stay close to legacy attribute names while still benefiting from
     * the registry-based resolution performed by [BuiltinAttributeUtils].
     *
     * @param attributeNames candidate attribute aliases
     * @param amount modifier amount
     * @param operation arithmetic operation
     * @param slot equipment slot
     * @param name local modifier path used for the generated key
     * @return immutable modifier specification
     */
    @JvmStatic
    fun mod(
        attributeNames: Array<String>,
        amount: Double,
        operation: AttributeModifier.Operation,
        slot: EquipmentSlot,
        name: String
    ): ModifierSpec = ModifierSpec(
        BuiltinAttributeUtils.attribute(*attributeNames),
        amount,
        operation,
        slot,
        name
    )

    /**
     * Builds a [ModifierSpec] from an already-resolved attribute.
     *
     * @param attribute target attribute
     * @param amount modifier amount
     * @param operation arithmetic operation
     * @param slot equipment slot
     * @param name local modifier path used for the generated key
     * @return immutable modifier specification
     */
    @JvmStatic
    fun mod(
        attribute: Attribute,
        amount: Double,
        operation: AttributeModifier.Operation,
        slot: EquipmentSlot,
        name: String
    ): ModifierSpec = ModifierSpec(
        attribute,
        amount,
        operation,
        slot,
        name
    )

    /**
     * Returns the preferred built-in armor trim material for a team preview.
     *
     * The mapping is purely a presentation convention used by the current project to visually distinguish teams in armor
     * templates and lobby previews.
     *
     * @param team 1-based team number, or `null`
     * @return preferred trim material for the supplied team
     */
    @JvmStatic
    fun trimMaterialForTeam(team: Int?): TrimMaterial = when (maxOf(1, team ?: 1)) {
        1 -> TrimMaterial.REDSTONE
        2 -> TrimMaterial.LAPIS
        3 -> TrimMaterial.GOLD
        4 -> TrimMaterial.EMERALD
        else -> TrimMaterial.QUARTZ
    }

    /**
     * Picks a leather armor tint for the supplied team id.
     *
     * The caller provides the actual palette values so jobs can define their own colour scheme while reusing the same
     * team-selection logic.
     *
     * @param team 1-based team number, or `null`
     * @param c1 colour used for team 1
     * @param c2 colour used for team 2
     * @param c3 colour used for team 3
     * @param c4 colour used for team 4
     * @return selected RGB colour value
     */
    @JvmStatic
    fun teamLeatherColor(
        team: Int?,
        c1: Int,
        c2: Int,
        c3: Int,
        c4: Int
    ): Int = when (maxOf(1, team ?: 1)) {
        1 -> c1
        2 -> c2
        3 -> c3
        4 -> c4
        else -> c1
    }

    /**
     * Creates a mutable lore list from plain-text lines.
     *
     * This is mainly a convenience helper for concise item-template declarations in job classes.
     *
     * @param lines lore lines in display order
     * @return mutable list containing the provided lines
     */
    @JvmStatic
    fun lore(vararg lines: String): MutableList<String> = lines.toMutableList()

    /**
     * Immutable description of an attribute modifier that should be attached to an item.
     *
     * The factory uses this tiny value object so job classes can declare armor modifiers as data and let [armor] apply
     * them in a consistent way.
     *
     * @property attribute target attribute
     * @property amount modifier amount
     * @property operation arithmetic operation
     * @property slot equipment slot that receives the modifier
     * @property name local path used when generating the modifier key
     */
    data class ModifierSpec(
        val attribute: Attribute,
        val amount: Double,
        val operation: AttributeModifier.Operation,
        val slot: EquipmentSlot,
        val name: String
    )

}
