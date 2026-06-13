package top.ourisland.creepersiarena.core.utils

import org.bukkit.attribute.Attributable
import org.bukkit.attribute.Attribute

/**
 * Small typed helpers for reading and writing entity attribute instances.
 *
 * The project targets one explicit Paper version, so callers pass the exact modern [Attribute] constant. Missing
 * attribute instances are still handled normally because not every [Attributable] exposes every attribute.
 */
object AttributeUtils {

    @JvmStatic
    fun baseValue(
        entity: Attributable,
        attribute: Attribute
    ): Double? = entity.getAttribute(attribute)?.baseValue

    @JvmStatic
    fun setBaseValue(
        entity: Attributable,
        value: Double,
        attribute: Attribute
    ): Boolean {
        val instance = entity.getAttribute(attribute) ?: return false
        instance.baseValue = value
        return true
    }

}
