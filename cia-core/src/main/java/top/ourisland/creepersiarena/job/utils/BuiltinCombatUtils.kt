package top.ourisland.creepersiarena.job.utils

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import top.ourisland.creepersiarena.game.player.PlayerSessionStore
import top.ourisland.creepersiarena.game.player.PlayerState

/**
 * Shared combat-oriented helpers used by the built-in jobs.
 *
 * The original datapack relied heavily on scoreboards and selector filters. In the Paper implementation those concerns
 * are translated into a small set of reusable helpers for:
 * - enemy filtering based on the live [PlayerSessionStore]
 * - short-range aim-line target acquisition
 * - safe forward blink placement
 * - damage application that respects team relationships
 *
 * The goal of this object is to keep job executors focused on gameplay intent instead of repeating the same entity and
 * arena checks in every skill.
 */
object BuiltinCombatUtils {

    @Volatile
    private var installedSessions: PlayerSessionStore? = null

    /**
     * Installs the active [PlayerSessionStore] used by the helper methods.
     *
     * Once installed, methods such as [nearbyOtherPlayers], [rayOtherPlayer] and [damage] can filter out players who are
     * not currently in-game or who belong to the same team as the source player.
     *
     * @param sessions session store representing the current game runtime
     */
    @JvmStatic
    fun installSessions(sessions: PlayerSessionStore) {
        installedSessions = sessions
    }

    /**
     * Finds the closest valid enemy roughly aligned with the source player's aim line.
     *
     * The method first collects nearby enemy players, then filters them by two constraints:
     * - their straight-line distance from the source must be within [range]
     * - their eye position must be within [maxDistanceFromRay] blocks of the source aim ray
     *
     * This makes it suitable for lock-on style skills that should reward aiming without requiring exact ray tracing.
     *
     * @param source source player
     * @param range maximum allowed source-to-target distance
     * @param maxDistanceFromRay maximum perpendicular distance from the aim ray
     * @return the closest matching enemy, or `null` when nothing intersects the aim corridor
     */
    @JvmStatic
    fun rayOtherPlayer(source: Player, range: Double, maxDistanceFromRay: Double): Player? {
        val origin = source.eyeLocation.toVector()
        val direction = source.eyeLocation.direction.normalize()

        return nearbyOtherPlayers(source, range + 2.0)
            .asSequence()
            .filter { it.location.distanceSquared(source.location) <= range * range }
            .filter { distanceToRay(origin, direction, it.eyeLocation.toVector()) <= maxDistanceFromRay }
            .minByOrNull { it.location.distanceSquared(source.location) }
    }

    /**
     * Returns nearby enemy players around [source].
     *
     * If [installSessions] has not been called yet, the method degrades gracefully to "all other nearby players". Once
     * a session store is available, the result excludes:
     * - the source player itself
     * - players without an active session entry
     * - players who are not in the [PlayerState.IN_GAME] state
     * - players on the same selected team
     *
     * @param source source player
     * @param radius search radius on all three axes
     * @return nearby players considered valid enemy targets
     */
    @JvmStatic
    fun nearbyOtherPlayers(source: Player, radius: Double): List<Player> =
        source.getNearbyEntities(radius, radius, radius)
            .asSequence()
            .mapNotNull { it as? Player }
            .filterNot { it == source }
            .filter { other ->
                val installed = installedSessions
                installed == null || isEnemy(installed, source, other)
            }
            .toList()

    /**
     * Returns whether [target] should be treated as an enemy of [source].
     *
     * This check is intentionally stricter than a simple team comparison. Both players must be present in the session
     * store and must already be in the in-game state; otherwise the method returns `false`.
     *
     * @param sessions active session store
     * @param source acting player
     * @param target entity being evaluated
     * @return `true` when the entity is a player on a different team and both players are actively in game
     */
    @JvmStatic
    fun isEnemy(sessions: PlayerSessionStore, source: Player, target: Entity): Boolean {
        val other = target as? Player ?: return false
        if (other == source) return false

        val sourceSession = sessions.get(source) ?: return false
        val targetSession = sessions.get(other) ?: return false
        if (sourceSession.state() != PlayerState.IN_GAME || targetSession.state() != PlayerState.IN_GAME) {
            return false
        }

        val sourceTeam = sourceSession.selectedTeam()
        val targetTeam = targetSession.selectedTeam()
        return sourceTeam == null || sourceTeam != targetTeam
    }

    /**
     * Finds the furthest safe forward blink destination up to [maxDistance].
     *
     * The algorithm walks backwards from [maxDistance] to `0.5` blocks in half-block steps and returns the first
     * location that has passable feet/head blocks and a solid supporting block below. When no safe location is found,
     * the player's current location is returned.
     *
     * @param player blinking player
     * @param maxDistance maximum desired travel distance
     * @return a location considered standable by the helper's block checks
     */
    @JvmStatic
    fun safeForwardBlink(player: Player, maxDistance: Double): Location {
        val world = player.world
        val direction = player.location.direction.setY(0).normalize()
        val base = player.location

        var distance = maxDistance
        while (distance >= 0.5) {
            val feet = base.clone().add(direction.clone().multiply(distance))
            feet.y = base.y
            if (isStandable(world, feet)) {
                return feet.add(0.0, 0.05, 0.0)
            }
            distance -= 0.5
        }

        return base.clone()
    }

    /**
     * Applies team-aware damage from [source] to [target].
     *
     * If a session store has been installed, friendly-fire damage is suppressed by [isEnemy]. When no session store is
     * available, the helper falls back to plain Paper damage with the supplied attacker.
     *
     * @param source attacking player, or `null` to skip damage
     * @param target target player, or `null` to skip damage
     * @param amount damage amount passed to Paper
     */
    @JvmStatic
    fun damage(source: Player?, target: Player?, amount: Double) {
        if (source == null || target == null) return
        val sessions = installedSessions
        if (sessions != null && !isEnemy(sessions, source, target)) return
        target.damage(amount, source)
    }

    /**
     * Applies the built-in hidden glowing effect helper to [player].
     *
     * This is primarily a convenience wrapper used by migrated skills that need a short visual reveal window while still
     * reusing the project's standard "ambient=false / particles=false / icon=false" effect policy.
     *
     * @param player player to highlight
     * @param ticks duration in ticks
     */
    @JvmStatic
    fun glow(player: Player, ticks: Int) {
        BuiltinStateUtils.applyHiddenEffect(player, org.bukkit.potion.PotionEffectType.GLOWING, ticks)
    }

    /** Returns the shortest distance between [point] and the infinite ray defined by [origin] and [direction]. */
    private fun distanceToRay(origin: Vector, direction: Vector, point: Vector): Double {
        val relative = point.clone().subtract(origin)
        val t = maxOf(0.0, relative.dot(direction))
        val closest = origin.clone().add(direction.clone().multiply(t))
        return closest.distance(point)
    }

    /** Returns whether [feet] is safe enough for a player-sized teleport destination. */
    private fun isStandable(world: World, feet: Location): Boolean {
        val feetBlock = world.getBlockAt(feet)
        val headBlock = world.getBlockAt(feet.clone().add(0.0, 1.0, 0.0))
        val belowBlock = world.getBlockAt(feet.clone().add(0.0, -1.0, 0.0))
        return feetBlock.isPassable && headBlock.isPassable && belowBlock.type.isSolid
    }

}
