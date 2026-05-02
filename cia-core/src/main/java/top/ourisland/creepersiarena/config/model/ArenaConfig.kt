package top.ourisland.creepersiarena.config.model

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.util.*

/**
 * Generic arena configuration loaded from arena.yml.
 *
 * Core owns only the common arena envelope. Mode-specific fields must stay in the arena settings section and be parsed
 * by the mode implementation that owns them.
 */
data class ArenaConfig(
    @get:JvmName("arenas")
    val arenas: Map<String, ArenaDef>,
) {

    companion object {

        @JvmStatic
        fun fromYaml(yml: YamlConfiguration): ArenaConfig {
            val root = yml.getConfigurationSection("arena") ?: return empty()

            val map = LinkedHashMap<String, ArenaDef>()
            for (id in root.getKeys(false)) {
                val sec = root.getConfigurationSection(id) ?: continue
                map[id] = ArenaDef.fromSection(id, sec)
            }
            return ArenaConfig(Collections.unmodifiableMap(map))
        }

        @JvmStatic
        fun empty(): ArenaConfig = ArenaConfig(mapOf())

        internal fun parseVecList(value: Any?): List<Vec3> {
            if (value !is List<*>) return listOf()
            if (value.size >= 3 && value.take(3).all { it is Number || it is String }) {
                return listOf(Vec3.fromList(value))
            }
            val out = ArrayList<Vec3>()
            for (item in value) {
                if (item is List<*>) {
                    out.add(Vec3.fromList(item))
                }
            }
            return Collections.unmodifiableList(out)
        }

    }

    fun get(id: String): ArenaDef? = arenas[id]

    data class ArenaDef(
        @get:JvmName("id") val id: String,
        @get:JvmName("nameKey") val nameKey: String,
        @get:JvmName("type") val type: String,
        @get:JvmName("location") val location: Vec3,
        @get:JvmName("range") val range: Range2D,
        @get:JvmName("spawnGroups") val spawnGroups: Map<String, List<Vec3>>,
        @get:JvmName("settings") val settings: ConfigurationSection?,
    ) {

        companion object {

            internal fun fromSection(id: String, sec: ConfigurationSection): ArenaDef {
                val nameKey = sec.getString("name", "cia.arena.$id") ?: "cia.arena.$id"
                val type = sec.getString("mode", sec.getString("type", "battle")) ?: "battle"
                val loc = Vec3.fromList(sec.getList("location"))
                val range = Range2D.fromSection(sec.getConfigurationSection("range"))
                val spawnGroups = parseSpawnGroups(sec)
                val settings = sec.getConfigurationSection("settings") ?: sec

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

            private fun parseSpawnGroups(sec: ConfigurationSection): Map<String, List<Vec3>> {
                val out = LinkedHashMap<String, List<Vec3>>()

                val spawns = sec.getConfigurationSection("spawns")
                if (spawns != null) {
                    for (key in spawns.getKeys(false)) {
                        val value = spawns.get(key)
                        putSpawnGroup(out, key, value)
                    }
                }

                if (out.isEmpty()) {
                    parseLegacySpawnpoint(sec.get("spawnpoint"), out)
                }

                return out
            }

            private fun parseLegacySpawnpoint(value: Any?, out: MutableMap<String, List<Vec3>>) {
                when (value) {
                    is List<*> -> putSpawnGroup(out, "default", value)
                    is ConfigurationSection -> {
                        for (key in value.getKeys(false)) {
                            putSpawnGroup(out, key, value.get(key))
                        }
                    }

                    is Map<*, *> -> {
                        for ((key, groupValue) in value) {
                            if (key == null) continue
                            putSpawnGroup(out, key.toString(), groupValue)
                        }
                    }
                }
            }

            private fun putSpawnGroup(out: MutableMap<String, List<Vec3>>, key: String, value: Any?) {
                val normalized = normalizeGroup(key)
                if (normalized.isEmpty()) return

                val list = parseVecList(value)
                if (list.isEmpty()) return

                out[normalized] = list
            }

            private fun normalizeGroup(key: String): String = key.trim().lowercase(Locale.ROOT)

        }

        fun mode(): String = type

        fun spawnpoints(): List<Vec3> = spawnGroups["default"] ?: listOf()

        fun teamSpawnpoints(): Map<String, Vec3> {
            val out = LinkedHashMap<String, Vec3>()
            for ((group, list) in spawnGroups) {
                if (group == "default" || list.isEmpty()) continue
                out[group] = list.first()
            }
            return Collections.unmodifiableMap(out)
        }

    }

    data class Vec3(
        @get:JvmName("x") val x: Int,
        @get:JvmName("y") val y: Int,
        @get:JvmName("z") val z: Int,
    ) {

        companion object {

            fun fromList(list: List<*>?): Vec3 {
                if (list == null || list.size < 3) return Vec3(0, 0, 0)
                return Vec3(
                    GlobalConfig.toInt(list[0]),
                    GlobalConfig.toInt(list[1]),
                    GlobalConfig.toInt(list[2]),
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

            fun fromSection(sec: ConfigurationSection?): Range2D {
                if (sec == null) return Range2D(-100, -100, 100, 100)

                val from = sec.getList("from", listOf(100, 100))
                val to = sec.getList("to", listOf(-100, -100))

                val x1 = GlobalConfig.toInt(from?.getOrNull(0))
                val z1 = GlobalConfig.toInt(from?.getOrNull(1))
                val x2 = GlobalConfig.toInt(to?.getOrNull(0))
                val z2 = GlobalConfig.toInt(to?.getOrNull(1))

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
