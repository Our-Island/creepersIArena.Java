package top.ourisland.creepersiarena.config.model

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import top.ourisland.creepersiarena.config.model.GlobalConfig.Range2D
import top.ourisland.creepersiarena.config.model.GlobalConfig.Vec3
import java.util.*

/**
 * Arena configuration model loaded from arena.yml.
 *
 * Keeps the same JVM-facing accessor names as the original Java `record`.
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
    }

    fun get(id: String): ArenaDef? = arenas[id]

    data class ArenaDef(
        @get:JvmName("id") val id: String,
        @get:JvmName("nameKey") val nameKey: String,
        @get:JvmName("type") val type: String,
        @get:JvmName("location") val location: Vec3,
        @get:JvmName("range") val range: Range2D,
        @get:JvmName("spawnpoints") val spawnpoints: List<Vec3>,
        @get:JvmName("teamSpawnpoints") val teamSpawnpoints: Map<String, Vec3>,
        @get:JvmName("redstoneBlocksRaw") val redstoneBlocksRaw: List<Any?>,
    ) {
        companion object {
            internal fun fromSection(id: String, sec: ConfigurationSection): ArenaDef {
                val nameKey = sec.getString("name", "cia.arena.$id") ?: "cia.arena.$id"
                val type = sec.getString("type", "battle") ?: "battle"

                val loc = Vec3.fromList(sec.getList("location"))
                val range = Range2D.fromSection(sec.getConfigurationSection("range"))

                val spObj = sec.get("spawnpoint")

                val listSpawn = ArrayList<Vec3>()
                val teamSpawn = LinkedHashMap<String, Vec3>()

                when (spObj) {
                    is List<*> -> {
                        for (o in spObj) {
                            if (o is List<*>) listSpawn.add(Vec3.fromList(o))
                        }
                    }

                    is ConfigurationSection -> {
                        for (k in spObj.getKeys(false)) {
                            teamSpawn[k] = Vec3.fromList(spObj.getList(k))
                        }
                    }

                    is Map<*, *> -> {
                        for ((k, v) in spObj) {
                            if (v is List<*>) teamSpawn[k.toString()] = Vec3.fromList(v)
                        }
                    }
                }

                val redstoneRaw = ArrayList<Any?>()
                val rs = sec.get("redstone-blocks")
                if (rs is List<*>) redstoneRaw.addAll(rs)

                return ArenaDef(
                    id,
                    nameKey,
                    type,
                    loc,
                    range,
                    Collections.unmodifiableList(listSpawn),
                    Collections.unmodifiableMap(teamSpawn),
                    Collections.unmodifiableList(redstoneRaw),
                )
            }
        }
    }
}
