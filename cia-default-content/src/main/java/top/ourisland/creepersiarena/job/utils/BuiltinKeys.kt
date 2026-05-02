package top.ourisland.creepersiarena.job.utils

import org.bukkit.NamespacedKey

/**
 * Produces [NamespacedKey] instances for the built-in job system.
 *
 * Built-in runtime data such as persistent-data markers, attribute modifier ids and other plugin-owned keys should live
 * under the stable CreepersIArena namespace. Centralising key creation here avoids scattering ad-hoc string literals throughout
 * the migrated job code and makes the namespace policy explicit.
 *
 * Addon content is expected to use its own plugin namespace; this object is therefore intentionally scoped to the main
 * CreepersIArena plugin only.
 */
object BuiltinKeys {

    /**
     * Creates a [NamespacedKey] owned by the stable CreepersIArena namespace.
     *
     * The [value] parameter is treated as the local key path. Callers typically pass concise identifiers such as
     * `weapon_damage`, `ysahan_whale_until` or similar plugin-internal paths.
     *
     * @param value plugin-local key path
     * @return namespaced key under the stable CreepersIArena namespace
     */
    @JvmStatic
    fun key(value: String): NamespacedKey =
        NamespacedKey("creepersiarena", value)

}
