package top.ourisland.creepersiarena.config.model

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.jspecify.annotations.Nullable
import java.util.*

/**
 * Global configuration model loaded from config.yml.
 *
 * This Kotlin version keeps the same JVM-facing accessor names as the original Java `record`
 * (e.g. `lang()`, `disabledJobs()`, `world().enablePortals()`), so existing Java/Kotlin callers
 * don't need any changes.
 */
data class GlobalConfig(
    @get:JvmName("lang") val lang: String,
    @get:JvmName("disabledJobs") val disabledJobs: Set<String>,
    @get:JvmName("lobbies") val lobbies: Map<String, Lobby>,
    @get:JvmName("game") val game: Game,
    @get:JvmName("ui") val ui: Ui,
    @get:JvmName("world") val world: World,
) {

    companion object {
        fun defaults(): GlobalConfig = GlobalConfig(
            "en_us",
            setOf(),
            mapOf(),
            Game.defaults(),
            Ui.defaults(),
            World.defaults(),
        )

        @JvmStatic
        fun fromYaml(yml: YamlConfiguration): GlobalConfig {
            val lang = yml.getString("lang", "en_us") ?: "en_us"

            val disabledJobs = HashSet(yml.getStringList("disabled-jobs"))

            // lobbies
            val lobbies = HashMap<String, Lobby>()
            val lobbiesSec = yml.getConfigurationSection("lobbies")
            if (lobbiesSec != null) {
                for (key in lobbiesSec.getKeys(false)) {
                    val sec = lobbiesSec.getConfigurationSection(key) ?: continue
                    lobbies[key] = Lobby.fromSection(sec)
                }
            }

            // game
            val game = Game.fromSection(yml.getConfigurationSection("game"))

            // ui
            val ui = Ui.fromSection(yml.getConfigurationSection("ui"))

            // world
            val world = World.fromSection(yml.getConfigurationSection("world"))

            return GlobalConfig(lang, disabledJobs, lobbies, game, ui, world)
        }

        private fun asDouble(list: List<*>?, idx: Int, def: Double): Double {
            if (list == null || list.size <= idx) return def
            val o = list[idx]
            if (o is Number) return o.toDouble()
            return try {
                o?.toString()?.toDouble() ?: def
            } catch (_: Exception) {
                def
            }
        }
    }

    // ---------------- sub models ----------------

    enum class JobSelectMode {
        HOTBAR,
        INVENTORY;

        companion object {
            fun fromConfig(s: String?): JobSelectMode {
                if (s == null) return HOTBAR
                return when (s.trim().lowercase(Locale.ROOT)) {
                    "inventory", "inv", "bag" -> INVENTORY
                    else -> HOTBAR
                }
            }
        }
    }

    data class Lobby(
        @get:JvmName("x") val x: Double,
        @get:JvmName("y") val y: Double,
        @get:JvmName("z") val z: Double,
        @get:JvmName("fromX") val fromX: Double,
        @get:JvmName("fromZ") val fromZ: Double,
        @get:JvmName("toX") val toX: Double,
        @get:JvmName("toZ") val toZ: Double,
        @get:JvmName("entry") val entry: @Nullable Entry?,
    ) {
        companion object {
            fun fromSection(sec: ConfigurationSection): Lobby {
                val loc = sec.getList("location", listOf(0, 100, 0))
                val from = sec.getList("range.from", listOf(100, 100))
                val to = sec.getList("range.to", listOf(-100, -100))

                val x = asDouble(loc, 0, 0.0)
                val y = asDouble(loc, 1, 100.0)
                val z = asDouble(loc, 2, 0.0)

                val fromX = asDouble(from, 0, 100.0)
                val fromZ = asDouble(from, 1, 100.0)
                val toX = asDouble(to, 0, -100.0)
                val toZ = asDouble(to, 1, -100.0)

                val entry = Entry.fromSection(sec.getConfigurationSection("entry"))
                return Lobby(x, y, z, fromX, fromZ, toX, toZ, entry)
            }
        }

        data class Entry(
            @get:JvmName("timeMs") val timeMs: Long,
            @get:JvmName("fromX") val fromX: Double,
            @get:JvmName("fromY") val fromY: Double,
            @get:JvmName("fromZ") val fromZ: Double,
            @get:JvmName("toX") val toX: Double,
            @get:JvmName("toY") val toY: Double,
            @get:JvmName("toZ") val toZ: Double,
        ) {
            companion object {
                fun fromSection(sec: ConfigurationSection?): @Nullable Entry? {
                    if (sec == null) return null
                    val time = sec.getLong("time", 0L)
                    if (time <= 0L) return null

                    val from = sec.getList("from", listOf(0, 0, 0))
                    val to = sec.getList("to", listOf(0, 0, 0))

                    val fromX = asDouble(from, 0, 0.0)
                    val fromY = asDouble(from, 1, 0.0)
                    val fromZ = asDouble(from, 2, 0.0)

                    val toX = asDouble(to, 0, 0.0)
                    val toY = asDouble(to, 1, 0.0)
                    val toZ = asDouble(to, 2, 0.0)

                    return Entry(time, fromX, fromY, fromZ, toX, toY, toZ)
                }
            }
        }
    }

    data class Game(
        @get:JvmName("disabledModes") val disabledModes: Set<String>,
        @get:JvmName("leaveDelaySeconds") val leaveDelaySeconds: Int,
        @get:JvmName("battle") val battle: Battle,
        @get:JvmName("steal") val steal: Steal,
    ) {
        companion object {
            fun fromSection(sec: ConfigurationSection?): Game {
                if (sec == null) return defaults()

                val disabled = HashSet(sec.getStringList("disabled-modes"))
                val leaveDelay = sec.getInt("leave-delay-seconds", 5).coerceAtLeast(0)

                val battle = Battle.fromSection(sec.getConfigurationSection("battle"))
                val steal = Steal.fromSection(sec.getConfigurationSection("steal"))

                return Game(disabled, leaveDelay, battle, steal)
            }

            fun defaults(): Game = Game(setOf(), 5, Battle.defaults(), Steal.defaults())
        }

        data class Battle(
            @get:JvmName("singleGameTimeSeconds") val singleGameTimeSeconds: Int,
            @get:JvmName("respawnTimeSeconds") val respawnTimeSeconds: Int,
            @get:JvmName("maxTeam") val maxTeam: Int,
            @get:JvmName("teamAutoBalancing") val teamAutoBalancing: Boolean,
            @get:JvmName("forceBalancing") val forceBalancing: Boolean,
        ) {
            companion object {
                fun fromSection(sec: ConfigurationSection?): Battle {
                    if (sec == null) return defaults()
                    return Battle(
                        sec.getInt("single-game-time", 600),
                        sec.getInt("respawn-time", 10),
                        sec.getInt("max-team", 4),
                        sec.getBoolean("team-auto-balancing", true),
                        sec.getBoolean("force-balancing", false),
                    )
                }

                fun defaults(): Battle = Battle(600, 10, 4, true, false)
            }
        }

        data class Steal(
            @get:JvmName("minPlayerToStart") val minPlayerToStart: Int,
            @get:JvmName("prepareTimeSeconds") val prepareTimeSeconds: Int,
            @get:JvmName("totalRound") val totalRound: Int,
            @get:JvmName("timePerRoundSeconds") val timePerRoundSeconds: Int,
        ) {
            companion object {
                fun fromSection(sec: ConfigurationSection?): Steal {
                    if (sec == null) return defaults()
                    return Steal(
                        sec.getInt("min-player-to-start", 2),
                        sec.getInt("prepare-time", 30),
                        sec.getInt("total-round", 10),
                        sec.getInt("time-per-round", 10),
                    )
                }

                fun defaults(): Steal = Steal(2, 30, 10, 10)
            }
        }
    }

    data class Ui(
        @get:JvmName("lobby") val lobby: LobbyUi,
    ) {
        companion object {
            fun fromSection(sec: ConfigurationSection?): Ui {
                if (sec == null) return defaults()
                return Ui(LobbyUi.fromSection(sec.getConfigurationSection("lobby")))
            }

            fun defaults(): Ui = Ui(LobbyUi.defaults())
        }
    }

    data class LobbyUi(
        @get:JvmName("jobSelectMode") val jobSelectMode: JobSelectMode,
        @get:JvmName("jobsPerPage") val jobsPerPage: Int,
    ) {
        companion object {
            fun fromSection(sec: ConfigurationSection?): LobbyUi {
                if (sec == null) return defaults()
                val mode = sec.getString("job-select-mode", "hotbar")
                val perPage = sec.getInt("jobs-per-page", 5)
                return LobbyUi(JobSelectMode.fromConfig(mode), perPage)
            }

            fun defaults(): LobbyUi = LobbyUi(JobSelectMode.HOTBAR, 5)
        }
    }

    data class World(
        @get:JvmName("enablePortals") val enablePortals: Boolean,
    ) {
        companion object {
            fun fromSection(sec: ConfigurationSection?): World {
                if (sec == null) return defaults()
                return World(sec.getBoolean("enable-portals", true))
            }

            fun defaults(): World = World(true)
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
                return Vec3(toInt(list[0]), toInt(list[1]), toInt(list[2]))
            }

            private fun toInt(o: Any?): Int {
                if (o is Number) return o.toInt()
                return try {
                    o?.toString()?.toInt() ?: 0
                } catch (_: Exception) {
                    0
                }
            }
        }
    }

    data class Range2D(
        @get:JvmName("minX") val minX: Int,
        @get:JvmName("maxX") val maxX: Int,
        @get:JvmName("minZ") val minZ: Int,
        @get:JvmName("maxZ") val maxZ: Int,
    ) {
        companion object {
            fun fromSection(sec: ConfigurationSection?): Range2D {
                if (sec == null) return Range2D(0, 0, 0, 0)
                val from = sec.getList("from")
                val to = sec.getList("to")
                val fx = if (from != null && from.size >= 2) toInt(from[0]) else 0
                val fz = if (from != null && from.size >= 2) toInt(from[1]) else 0
                val tx = if (to != null && to.size >= 2) toInt(to[0]) else 0
                val tz = if (to != null && to.size >= 2) toInt(to[1]) else 0
                return Range2D(
                    minOf(fx, tx),
                    maxOf(fx, tx),
                    minOf(fz, tz),
                    maxOf(fz, tz),
                )
            }

            private fun toInt(o: Any?): Int {
                if (o is Number) return o.toInt()
                return try {
                    o?.toString()?.toInt() ?: 0
                } catch (_: Exception) {
                    0
                }
            }
        }

        fun contains(x: Int, z: Int): Boolean = x in minX..maxX && z in minZ..maxZ
    }

}
