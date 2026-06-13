package top.ourisland.creepersiarena.core.config.model

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import top.ourisland.creepersiarena.api.config.StrictConfig
import top.ourisland.creepersiarena.api.game.arena.ArenaId
import top.ourisland.creepersiarena.api.game.mode.GameModeId
import java.util.*
import java.util.regex.Pattern

/**
 * Generic arena configuration loaded from arena.yml.
 *
 * Core owns only the common arena envelope. Mode-specific fields stay in the arena settings section and are parsed by
 * the mode implementation that owns them.
 */
data class ArenaConfig(
    @get:JvmName("arenas")
    val arenas: Map<ArenaId, ArenaDef>,
) {

    companion object {

        private val LOCAL_ID: Pattern = Pattern.compile("[a-z0-9][a-z0-9_-]*")

        @JvmStatic
        fun fromYaml(yml: YamlConfiguration): ArenaConfig {
            val root = StrictConfig.section(yml, "arena", "arena") ?: return empty()

            val map = LinkedHashMap<ArenaId, ArenaDef>()
            for (rawId in root.getKeys(false)) {
                val id = try {
                    ArenaId.parse(rawId)
                } catch (exception: IllegalArgumentException) {
                    throw IllegalArgumentException("Invalid arena id at arena.$rawId", exception)
                }
                val sec = StrictConfig.section(root, rawId, "arena.$rawId")
                    ?: throw IllegalArgumentException("Missing arena section at arena.$rawId")
                val previous = map.putIfAbsent(id, ArenaDef.fromSection(id, sec))
                require(previous == null) { "Duplicate arena id: $id" }
            }
            return ArenaConfig(Collections.unmodifiableMap(map))
        }

        @JvmStatic
        fun empty(): ArenaConfig = ArenaConfig(mapOf())

        internal fun parseVecList(value: Any?, path: String): List<Vec3> {
            if (value !is List<*>) {
                throw IllegalArgumentException("Invalid value at $path: expected coordinate list")
            }
            if (value.isEmpty()) {
                throw IllegalArgumentException("Invalid value at $path: coordinate list must not be empty")
            }
            if (value.size >= 3 && value.take(3).all { isInteger(it) }) {
                return listOf(Vec3.fromList(value, path))
            }

            val out = ArrayList<Vec3>(value.size)
            for ((index, item) in value.withIndex()) {
                if (item !is List<*>) {
                    throw IllegalArgumentException("Invalid value at $path[$index]: expected coordinate list")
                }
                out.add(Vec3.fromList(item, "$path[$index]"))
            }
            return Collections.unmodifiableList(out)
        }

        private fun isInteger(value: Any?): Boolean = when (value) {
            is Byte, is Short, is Int -> true
            is Long -> value in Int.MIN_VALUE..Int.MAX_VALUE
            else -> false
        }

        internal fun integer(value: Any?, path: String): Int = when (value) {
            is Byte, is Short, is Int -> (value as Number).toInt()
            is Long -> if (value in Int.MIN_VALUE..Int.MAX_VALUE) value.toInt() else {
                throw IllegalArgumentException("Invalid value at $path: integer is out of range")
            }

            else -> throw IllegalArgumentException("Invalid value at $path: expected integer, got '$value'")
        }

        internal fun validateLocalId(value: String, path: String): String {
            if (!LOCAL_ID.matcher(value).matches()) {
                throw IllegalArgumentException("Invalid local id at $path: $value")
            }
            return value
        }

    }

    fun get(id: ArenaId): ArenaDef? = arenas[id]

    data class ArenaDef(
        @get:JvmName("id") val id: ArenaId,
        @get:JvmName("nameKey") val nameKey: String,
        @get:JvmName("type") val type: GameModeId,
        @get:JvmName("location") val location: Vec3,
        @get:JvmName("range") val range: Range2D,
        @get:JvmName("spawnGroups") val spawnGroups: Map<String, List<Vec3>>,
        @get:JvmName("settings") val settings: ConfigurationSection?,
    ) {

        companion object {

            internal fun fromSection(id: ArenaId, sec: ConfigurationSection): ArenaDef {
                val base = "arena.${id.value()}"
                val nameKey = StrictConfig.string(sec, "name", "cia.arena.${id.value()}", "$base.name")
                    ?: "cia.arena.${id.value()}"
                require(nameKey.isNotBlank()) { "$base.name must not be blank" }

                val rawMode = StrictConfig.string(sec, "mode", null, "$base.mode")
                    ?: throw IllegalArgumentException("$base.mode must contain a namespaced mode id")
                val type = try {
                    GameModeId.parse(rawMode)
                } catch (exception: IllegalArgumentException) {
                    throw IllegalArgumentException("Invalid mode id at $base.mode: $rawMode", exception)
                }

                val loc = Vec3.fromList(
                    StrictConfig.list(sec, "location", listOf(0, 0, 0), "$base.location"),
                    "$base.location"
                )
                val range = Range2D.fromSection(StrictConfig.section(sec, "range", "$base.range"), "$base.range")
                val spawnGroups = parseSpawnGroups(sec, base)
                val settings = StrictConfig.section(sec, "settings", "$base.settings")

                return ArenaDef(
                    id,
                    nameKey,
                    type,
                    loc,
                    range,
                    Collections.unmodifiableMap(spawnGroups),
                    settings,
                )
            }

            private fun parseSpawnGroups(sec: ConfigurationSection, base: String): Map<String, List<Vec3>> {
                val out = LinkedHashMap<String, List<Vec3>>()
                val spawns = StrictConfig.section(sec, "spawns", "$base.spawns") ?: return out

                for (key in spawns.getKeys(false)) {
                    val group = validateLocalId(key, "$base.spawns.$key")
                    val value = spawns.get(key)
                    val list = parseVecList(value, "$base.spawns.$key")
                    val previous = out.putIfAbsent(group, list)
                    require(previous == null) { "Duplicate spawn group at $base.spawns.$group" }
                }
                return out
            }

        }

        fun mode(): GameModeId = type

    }

    data class Vec3(
        @get:JvmName("x") val x: Int,
        @get:JvmName("y") val y: Int,
        @get:JvmName("z") val z: Int,
    ) {

        companion object {

            fun fromList(list: List<*>?, path: String): Vec3 {
                if (list == null || list.size != 3) {
                    throw IllegalArgumentException("Invalid value at $path: expected exactly three integer coordinates")
                }
                return Vec3(
                    integer(list[0], "$path[0]"),
                    integer(list[1], "$path[1]"),
                    integer(list[2], "$path[2]"),
                )
            }

        }

    }

    data class Range2D(
        @get:JvmName("minX") val minX: Int,
        @get:JvmName("minZ") val minZ: Int,
        @get:JvmName("maxX") val maxX: Int,
        @get:JvmName("maxZ") val maxZ: Int,
    ) {

        companion object {

            fun fromSection(sec: ConfigurationSection?, path: String): Range2D {
                if (sec == null) return Range2D(-100, -100, 100, 100)

                val from = StrictConfig.list(sec, "from", listOf(100, 100), "$path.from")
                val to = StrictConfig.list(sec, "to", listOf(-100, -100), "$path.to")
                if (from.size != 2 || to.size != 2) {
                    throw IllegalArgumentException("$path.from and $path.to must each contain exactly two integers")
                }

                val x1 = integer(from[0], "$path.from[0]")
                val z1 = integer(from[1], "$path.from[1]")
                val x2 = integer(to[0], "$path.to[0]")
                val z2 = integer(to[1], "$path.to[1]")

                return Range2D(
                    minOf(x1, x2),
                    minOf(z1, z2),
                    maxOf(x1, x2),
                    maxOf(z1, z2),
                )
            }

        }

    }

}
