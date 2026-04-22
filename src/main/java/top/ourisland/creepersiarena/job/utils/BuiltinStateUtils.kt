package top.ourisland.creepersiarena.job.utils

import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Helpers for short-lived built-in skill state.
 *
 * Many migrated skills need temporary "active until" windows or short hidden potion effects. Instead of duplicating the
 * same persistent-data and potion boilerplate in every executor/listener pair, this object centralises those patterns.
 *
 * ## Timed state model
 * Timed states are stored as absolute epoch-millisecond expiry timestamps inside a [PersistentDataContainer]. A state is
 * considered active when the stored timestamp is strictly greater than the current wall-clock time.
 *
 * ## Hidden effect policy
 * [applyHiddenEffect] applies effects with ambient particles, visible particles and inventory icons disabled. This keeps
 * built-in combat feedback intentional and avoids cluttering the player's HUD for very short mechanics windows.
 */
object BuiltinStateUtils {

    /**
     * Stores a future expiry timestamp for a temporary state.
     *
     * @param container target persistent-data container
     * @param key key under which the expiry should be stored
     * @param durationMillis duration added to the current wall-clock time
     * @return stored expiry timestamp in epoch milliseconds
     */
    @JvmStatic
    fun markTimed(container: PersistentDataContainer, key: NamespacedKey, durationMillis: Long): Long {
        val until = System.currentTimeMillis() + durationMillis
        container.set(key, PersistentDataType.LONG, until)
        return until
    }

    /**
     * Reads the expiry timestamp of a temporary state.
     *
     * @param container source persistent-data container
     * @param key state key
     * @return stored epoch-millisecond timestamp, or `null` when the state is absent
     */
    @JvmStatic
    fun timedUntil(container: PersistentDataContainer, key: NamespacedKey): Long? =
        container.get(key, PersistentDataType.LONG)

    /**
     * Returns whether the temporary state identified by [key] is still active.
     *
     * @param container source persistent-data container
     * @param key state key
     * @return `true` when a stored expiry exists and lies in the future
     */
    @JvmStatic
    fun isTimedActive(container: PersistentDataContainer, key: NamespacedKey): Boolean =
        (timedUntil(container, key) ?: Long.MIN_VALUE) > System.currentTimeMillis()

    /**
     * Removes a previously stored temporary state.
     *
     * @param container target persistent-data container
     * @param key state key to remove
     */
    @JvmStatic
    fun clearTimed(container: PersistentDataContainer, key: NamespacedKey) {
        container.remove(key)
    }

    /**
     * Extends an already-active timed state.
     *
     * If the state does not exist or has already expired, the container is left unchanged and `null` is returned.
     *
     * @param container target persistent-data container
     * @param key state key
     * @param extraMillis additional milliseconds to append to the existing expiry
     * @return new expiry timestamp, or `null` when the state was absent or already expired
     */
    @JvmStatic
    fun extendTimedIfActive(
        container: PersistentDataContainer,
        key: NamespacedKey,
        extraMillis: Long
    ): Long? {
        val until = timedUntil(container, key) ?: return null
        if (until <= System.currentTimeMillis()) return null
        val extended = until + extraMillis
        container.set(key, PersistentDataType.LONG, extended)
        return extended
    }

    /**
     * Applies a short hidden potion effect to [entity].
     *
     * The effect is marked ambient and has particles/icons disabled so it can be used for gameplay mechanics without
     * noisy client-side rendering.
     *
     * @param entity target entity
     * @param type potion effect type
     * @param ticks duration in ticks
     * @param amplifier effect amplifier, default `0`
     */
    @JvmStatic
    @JvmOverloads
    fun applyHiddenEffect(
        entity: LivingEntity,
        type: PotionEffectType,
        ticks: Int,
        amplifier: Int = 0
    ) {
        entity.addPotionEffect(
            PotionEffect(
                type,
                ticks,
                amplifier,
                true,
                false,
                false
            )
        )
    }

}
