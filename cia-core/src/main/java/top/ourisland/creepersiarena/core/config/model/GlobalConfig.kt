package top.ourisland.creepersiarena.core.config.model

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.jspecify.annotations.Nullable
import top.ourisland.creepersiarena.api.config.IGameConfigView
import top.ourisland.creepersiarena.api.config.StrictConfig
import top.ourisland.creepersiarena.api.database.DatabaseType
import top.ourisland.creepersiarena.api.game.mode.GameModeId
import top.ourisland.creepersiarena.api.identity.CiaConfigPaths
import top.ourisland.creepersiarena.api.job.JobId
import java.util.*

/**
 * Global configuration model loaded from config.yml.
 *
 * Core keeps only platform-owned settings here. Mode-owned settings are exposed as generic sections through
 * [IGameConfigView] and are parsed by the mode implementation that owns them.
 */
data class GlobalConfig(
    @get:JvmName("lang") val lang: String,
    @get:JvmName("disabledJobs") val disabledJobs: Set<JobId>,
    @get:JvmName("lobbies") val lobbies: Map<String, Lobby>,
    @get:JvmName("game") val game: Game,
    @get:JvmName("ui") val ui: Ui,
    @get:JvmName("world") val world: World,
    @get:JvmName("database") val database: Database,
    private val modesRoot: ConfigurationSection?,
) : IGameConfigView {

    companion object {

        fun defaults(): GlobalConfig = GlobalConfig(
            "en_us",
            setOf(),
            mapOf(),
            Game.defaults(),
            Ui.defaults(),
            World.defaults(),
            Database.defaults(),
            null,
        )

        @JvmStatic
        fun fromYaml(yml: YamlConfiguration): GlobalConfig {
            val lang = StrictConfig.string(yml, "lang", "en_us", "lang") ?: "en_us"
            require(lang.isNotBlank()) { "lang must not be blank" }
            val disabledJobs = parseIds(
                StrictConfig.stringList(yml, "disabled-jobs", emptyList(), "disabled-jobs"),
                "disabled-jobs",
                JobId::parse
            )

            val lobbies = HashMap<String, Lobby>()
            val lobbiesSec = StrictConfig.section(yml, "lobbies", "lobbies")
            if (lobbiesSec != null) {
                for (key in lobbiesSec.getKeys(false)) {
                    val sec = StrictConfig.section(lobbiesSec, key, "lobbies.$key")
                        ?: throw IllegalArgumentException("Missing lobby section at lobbies.$key")
                    lobbies[key] = Lobby.fromSection(key, sec)
                }
            }

            val gameSec = StrictConfig.section(yml, "game", "game")
            val game = Game.fromSection(gameSec)
            val modesRoot = StrictConfig.section(gameSec, "modes", "game.modes")

            val ui = Ui.fromSection(StrictConfig.section(yml, "ui", "ui"))
            val world = World.fromSection(StrictConfig.section(yml, "world", "world"))
            val database = Database.fromSection(StrictConfig.section(yml, "database", "database"))

            return GlobalConfig(
                lang,
                Collections.unmodifiableSet(disabledJobs),
                Collections.unmodifiableMap(lobbies),
                game,
                ui,
                world,
                database,
                modesRoot,
            )
        }

        private fun <T> parseIds(
            values: List<String>,
            path: String,
            parser: (String) -> T
        ): Set<T> {
            val out = LinkedHashSet<T>()
            for (value in values) {
                try {
                    out.add(parser(value))
                } catch (exception: IllegalArgumentException) {
                    throw IllegalArgumentException("Invalid namespaced id at $path: $value", exception)
                }
            }
            return out
        }

        internal fun asDouble(
            list: List<*>?,
            idx: Int,
            def: Double
        ): Double {
            if (list == null || list.size <= idx) return def
            val value = list[idx]
            if (value is Number) return value.toDouble()
            throw IllegalArgumentException("Expected a numeric coordinate at index $idx, got '$value'")
        }

        internal fun toInt(o: Any?): Int {
            if (o is Number) return o.toInt()
            throw IllegalArgumentException("Expected an integer coordinate, got '$o'")
        }

    }

    override fun isModeDisabled(modeId: GameModeId): Boolean = game.disabledModes.contains(modeId)

    override fun leaveDelaySeconds(): Int = game.leaveDelaySeconds

    override fun modeSection(modeId: GameModeId): ConfigurationSection? {
        val key = CiaConfigPaths.section(modeId)
        return StrictConfig.section(modesRoot, key, "game.modes.$key")
    }

    enum class JobSelectMode {

        HOTBAR,
        INVENTORY;

        companion object {

            fun fromConfig(s: String?): JobSelectMode {
                if (s == null) return HOTBAR
                return try {
                    valueOf(s.trim().uppercase(Locale.ROOT))
                } catch (exception: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Invalid value at ui.lobby.job-select-mode: expected HOTBAR or INVENTORY, got '$s'",
                        exception
                    )
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

            fun fromSection(id: String, sec: ConfigurationSection): Lobby {
                val base = "lobbies.$id"
                val loc = StrictConfig.list(sec, "location", listOf(0, 100, 0), "$base.location")
                val from = StrictConfig.list(sec, "range.from", listOf(100, 100), "$base.range.from")
                val to = StrictConfig.list(sec, "range.to", listOf(-100, -100), "$base.range.to")

                val x = asDouble(loc, 0, 0.0)
                val y = asDouble(loc, 1, 100.0)
                val z = asDouble(loc, 2, 0.0)

                val fromX = asDouble(from, 0, 100.0)
                val fromZ = asDouble(from, 1, 100.0)
                val toX = asDouble(to, 0, -100.0)
                val toZ = asDouble(to, 1, -100.0)

                val entry = Entry.fromSection(
                    StrictConfig.section(sec, "entry", "$base.entry"),
                    "$base.entry"
                )
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

                fun fromSection(sec: ConfigurationSection?, path: String): @Nullable Entry? {
                    if (sec == null) return null
                    val time = StrictConfig.longValue(sec, "time", 0L, "$path.time")
                    require(time >= 0L) { "$path.time must be >= 0, got $time" }
                    if (time == 0L) return null

                    val from = StrictConfig.list(sec, "from", listOf(0, 0, 0), "$path.from")
                    val to = StrictConfig.list(sec, "to", listOf(0, 0, 0), "$path.to")

                    return Entry(
                        time,
                        asDouble(from, 0, 0.0),
                        asDouble(from, 1, 0.0),
                        asDouble(from, 2, 0.0),
                        asDouble(to, 0, 0.0),
                        asDouble(to, 1, 0.0),
                        asDouble(to, 2, 0.0),
                    )
                }

            }

        }

    }

    data class Game(
        @get:JvmName("disabledModes") val disabledModes: Set<GameModeId>,
        @get:JvmName("leaveDelaySeconds") val leaveDelaySeconds: Int,
        @get:JvmName("defaultMode") val defaultMode: @Nullable GameModeId?,
    ) {

        companion object {

            fun fromSection(sec: ConfigurationSection?): Game {
                if (sec == null) return defaults()
                return Game(
                    disabledModes = Collections.unmodifiableSet(
                        parseIds(
                            StrictConfig.stringList(
                                sec,
                                "disabled-modes",
                                emptyList(),
                                "game.disabled-modes"
                            ),
                            "game.disabled-modes",
                            GameModeId::parse
                        )
                    ),
                    leaveDelaySeconds = StrictConfig.integer(
                        sec,
                        "leave-delay-seconds",
                        5,
                        "game.leave-delay-seconds"
                    ).also {
                        require(it >= 0) { "game.leave-delay-seconds must be >= 0, got $it" }
                    },
                    defaultMode = StrictConfig.string(sec, "default-mode", null, "game.default-mode")
                        ?.let { raw ->
                            require(raw.isNotBlank()) { "game.default-mode must not be blank" }
                            try {
                                GameModeId.parse(raw)
                            } catch (exception: IllegalArgumentException) {
                                throw IllegalArgumentException(
                                    "Invalid namespaced id at game.default-mode: $raw",
                                    exception
                                )
                            }
                        },
                )
            }

            fun defaults(): Game = Game(
                disabledModes = setOf(),
                leaveDelaySeconds = 5,
                defaultMode = null
            )

        }

    }

    data class Ui(
        @get:JvmName("lobby") val lobby: LobbyUi,
    ) {

        companion object {

            fun fromSection(sec: ConfigurationSection?): Ui {
                if (sec == null) return defaults()
                return Ui(LobbyUi.fromSection(StrictConfig.section(sec, "lobby", "ui.lobby")))
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
                val mode = StrictConfig.string(sec, "job-select-mode", "hotbar", "ui.lobby.job-select-mode")
                val perPage = StrictConfig.integer(sec, "jobs-per-page", 5, "ui.lobby.jobs-per-page")
                require(perPage > 0) { "ui.lobby.jobs-per-page must be positive, got $perPage" }
                return LobbyUi(JobSelectMode.fromConfig(mode), perPage)
            }

            fun defaults(): LobbyUi = LobbyUi(
                jobSelectMode = JobSelectMode.HOTBAR,
                jobsPerPage = 5,
            )

        }

    }

    data class Database(
        @get:JvmName("type") val type: DatabaseType,
        @get:JvmName("tablePrefix") val tablePrefix: String,
        @get:JvmName("host") val host: String,
        @get:JvmName("port") val port: Int,
        @get:JvmName("database") val database: String,
        @get:JvmName("username") val username: String,
        @get:JvmName("password") val password: String,
        @get:JvmName("file") val file: String,
        @get:JvmName("parameters") val parameters: Map<String, String>,
        @get:JvmName("poolSize") val poolSize: Int,
        @get:JvmName("connectionTimeoutMs") val connectionTimeoutMs: Long,
        @get:JvmName("busyTimeoutMs") val busyTimeoutMs: Int,
        @get:JvmName("journalMode") val journalMode: String,
        @get:JvmName("synchronous") val synchronous: String,
        @get:JvmName("executorThreads") val executorThreads: Int,
        @get:JvmName("executorQueueSize") val executorQueueSize: Int,
        @get:JvmName("validateMigrationChecksum") val validateMigrationChecksum: Boolean,
    ) {

        companion object {

            fun fromSection(sec: ConfigurationSection?): Database {
                if (sec == null) return defaults()

                val type = DatabaseType.parse(
                    StrictConfig.string(sec, "type", "sqlite", "database.type")
                )
                val tablePrefix = sanitizeTablePrefix(
                    StrictConfig.string(sec, "table-prefix", "cia_", "database.table-prefix")
                )
                val typeKey = type.name.lowercase(Locale.ROOT)
                val typed = StrictConfig.section(sec, typeKey, "database.$typeKey")
                val typedPath = "database.$typeKey"

                val host = StrictConfig.string(typed, "host", "localhost", "$typedPath.host") ?: "localhost"

                val defaultPort = when (type) {
                    DatabaseType.MYSQL -> 3306
                    DatabaseType.POSTGRESQL -> 5432
                    DatabaseType.H2 -> 0
                    DatabaseType.SQLITE -> 0
                }
                val port = StrictConfig.integer(typed, "port", defaultPort, "$typedPath.port")
                if ((type == DatabaseType.MYSQL || type == DatabaseType.POSTGRESQL) && port !in 1..65535) {
                    throw IllegalArgumentException("$typedPath.port must be between 1 and 65535, got $port")
                }

                val database = StrictConfig.string(
                    typed,
                    "database",
                    "creepersiarena",
                    "$typedPath.database"
                ) ?: "creepersiarena"
                val username = StrictConfig.string(typed, "username", "", "$typedPath.username") ?: ""
                val password = StrictConfig.string(typed, "password", "", "$typedPath.password") ?: ""
                val file = StrictConfig.string(
                    typed,
                    "file",
                    defaultFile(type),
                    "$typedPath.file"
                ) ?: defaultFile(type)
                val parameters = parseParameters(
                    StrictConfig.section(typed, "parameters", "$typedPath.parameters")
                )

                val poolSize = positive(
                    StrictConfig.integer(
                        typed,
                        "pool-size",
                        defaultPoolSize(type),
                        "$typedPath.pool-size"
                    ),
                    "$typedPath.pool-size"
                )
                val connectionTimeoutMs = positiveLong(
                    StrictConfig.longValue(
                        typed,
                        "connection-timeout-ms",
                        5000L,
                        "$typedPath.connection-timeout-ms"
                    ),
                    "$typedPath.connection-timeout-ms"
                )
                val busyTimeoutMs = positive(
                    StrictConfig.integer(
                        typed,
                        "busy-timeout-ms",
                        5000,
                        "$typedPath.busy-timeout-ms"
                    ),
                    "$typedPath.busy-timeout-ms"
                )
                val journalMode = enumValue(
                    StrictConfig.string(typed, "journal-mode", "WAL", "$typedPath.journal-mode") ?: "WAL",
                    setOf("DELETE", "TRUNCATE", "PERSIST", "MEMORY", "WAL", "OFF"),
                    "$typedPath.journal-mode"
                )
                val synchronous = enumValue(
                    StrictConfig.string(typed, "synchronous", "NORMAL", "$typedPath.synchronous") ?: "NORMAL",
                    setOf("OFF", "NORMAL", "FULL", "EXTRA"),
                    "$typedPath.synchronous"
                )

                val executorSec = StrictConfig.section(sec, "executor", "database.executor")
                val executorThreads = positive(
                    StrictConfig.integer(executorSec, "threads", 2, "database.executor.threads"),
                    "database.executor.threads"
                )
                val executorQueueSize = positive(
                    StrictConfig.integer(executorSec, "queue-size", 10000, "database.executor.queue-size"),
                    "database.executor.queue-size"
                )

                val migrationSec = StrictConfig.section(sec, "migrations", "database.migrations")
                val validateChecksum = StrictConfig.bool(
                    migrationSec,
                    "validate-checksum",
                    true,
                    "database.migrations.validate-checksum"
                )

                return Database(
                    type,
                    tablePrefix,
                    host,
                    port,
                    database,
                    username,
                    password,
                    file,
                    parameters,
                    poolSize,
                    connectionTimeoutMs,
                    busyTimeoutMs,
                    journalMode,
                    synchronous,
                    executorThreads,
                    executorQueueSize,
                    validateChecksum,
                )
            }

            fun defaults(): Database = Database(
                type = DatabaseType.SQLITE,
                tablePrefix = "cia_",
                host = "localhost",
                port = 0,
                database = "creepersiarena",
                username = "",
                password = "",
                file = "database/creepersiarena.db",
                parameters = mapOf(),
                poolSize = 1,
                connectionTimeoutMs = 5000L,
                busyTimeoutMs = 5000,
                journalMode = "WAL",
                synchronous = "NORMAL",
                executorThreads = 2,
                executorQueueSize = 10000,
                validateMigrationChecksum = true,
            )

            private fun defaultFile(type: DatabaseType): String = when (type) {
                DatabaseType.H2 -> "database/creepersiarena"
                else -> "database/creepersiarena.db"
            }

            private fun defaultPoolSize(type: DatabaseType): Int = when (type) {
                DatabaseType.SQLITE -> 1
                DatabaseType.H2 -> 2
                else -> 10
            }

            private fun positive(value: Int, path: String): Int {
                if (value > 0) return value
                throw IllegalArgumentException("$path must be positive, got $value")
            }

            private fun positiveLong(value: Long, path: String): Long {
                if (value > 0L) return value
                throw IllegalArgumentException("$path must be positive, got $value")
            }

            private fun parseParameters(sec: ConfigurationSection?): Map<String, String> {
                if (sec == null) return mapOf()

                val out = LinkedHashMap<String, String>()
                for (key in sec.getKeys(false)) {
                    val value = sec.get(key)
                        ?: throw IllegalArgumentException("database parameters must not contain null: $key")
                    if (value !is String && value !is Number && value !is Boolean) {
                        throw IllegalArgumentException(
                            "Invalid value at database parameters.$key: expected scalar, got ${value::class.java.simpleName}"
                        )
                    }
                    out[key] = value.toString()
                }

                return Collections.unmodifiableMap(out)
            }

            private fun enumValue(raw: String, allowed: Set<String>, path: String): String {
                val value = raw.trim().uppercase(Locale.ROOT)
                if (value in allowed) return value
                throw IllegalArgumentException(
                    "Invalid value at $path: expected one of ${allowed.joinToString()}, got '$raw'"
                )
            }

            private fun sanitizeTablePrefix(raw: String?): String {
                val text = raw?.trim() ?: "cia_"
                require(text.isNotBlank() && text.matches(Regex("[A-Za-z0-9_]+"))) {
                    "Invalid value at database.table-prefix: expected [A-Za-z0-9_]+, got '$raw'"
                }
                return text
            }

        }

    }

    data class World(
        @get:JvmName("portalsEnabled") val portalsEnabled: Boolean,
    ) {

        companion object {

            fun fromSection(sec: ConfigurationSection?): World {
                if (sec == null) return defaults()
                return World(StrictConfig.bool(sec, "portals-enabled", false, "world.portals-enabled"))
            }

            fun defaults(): World = World(false)

        }

    }

}
