package top.ourisland.creepersiarena.core.config.model

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.jspecify.annotations.Nullable
import top.ourisland.creepersiarena.api.config.IGameConfigView
import top.ourisland.creepersiarena.api.database.DatabaseType
import top.ourisland.creepersiarena.api.game.mode.GameModeId
import top.ourisland.creepersiarena.api.identity.CiaKey
import top.ourisland.creepersiarena.api.identity.CiaNamespace
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
    private val modeSections: Map<GameModeId, ConfigurationSection>,
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
            mapOf(),
        )

        @JvmStatic
        fun fromYaml(yml: YamlConfiguration): GlobalConfig {
            val lang = yml.getString("lang", "en_us") ?: "en_us"
            val disabledJobs = parseIds(yml.getStringList("disabled-jobs"), "disabled-jobs", JobId::parse)

            val lobbies = HashMap<String, Lobby>()
            val lobbiesSec = yml.getConfigurationSection("lobbies")
            if (lobbiesSec != null) {
                for (key in lobbiesSec.getKeys(false)) {
                    val sec = lobbiesSec.getConfigurationSection(key) ?: continue
                    lobbies[key] = Lobby.fromSection(sec)
                }
            }

            val gameSec = yml.getConfigurationSection("game")
            val game = Game.fromSection(gameSec)
            val modeSections = collectModeSections(gameSec)

            val ui = Ui.fromSection(yml.getConfigurationSection("ui"))
            val world = World.fromSection(yml.getConfigurationSection("world"))
            val database = Database.fromSection(yml.getConfigurationSection("database"))

            return GlobalConfig(
                lang,
                Collections.unmodifiableSet(disabledJobs),
                Collections.unmodifiableMap(lobbies),
                game,
                ui,
                world,
                database,
                Collections.unmodifiableMap(modeSections),
            )
        }

        private fun collectModeSections(
            gameSec: ConfigurationSection?
        ): Map<GameModeId, ConfigurationSection> {
            if (gameSec == null) return mapOf()
            val modesSec = gameSec.getConfigurationSection("modes") ?: return mapOf()
            val out = LinkedHashMap<GameModeId, ConfigurationSection>()
            for (namespace in modesSec.getKeys(false)) {
                val namespaceSection = modesSec.getConfigurationSection(namespace) ?: continue
                collectModeSections(namespace, namespaceSection, "", out)
            }
            return out
        }

        private fun collectModeSections(
            namespace: String,
            section: ConfigurationSection,
            path: String,
            out: MutableMap<GameModeId, ConfigurationSection>
        ) {
            // A mode section is a leaf from the identity perspective; its children are mode-owned settings.
            if (path.isNotEmpty()) {
                val id = try {
                    GameModeId.of(
                        CiaKey.of(
                            CiaNamespace.parse(namespace),
                            path.replace('.', '/')
                        )
                    )
                } catch (exception: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Invalid game mode config path game.modes.$namespace.$path",
                        exception
                    )
                }
                out[id] = section
                return
            }
            for (key in section.getKeys(false)) {
                val child = section.getConfigurationSection(key) ?: continue
                collectModeSections(namespace, child, key, out)
            }
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
            val o = list[idx]
            if (o is Number) return o.toDouble()
            return try {
                o?.toString()?.toDouble() ?: def
            } catch (_: Exception) {
                def
            }
        }

        internal fun toInt(o: Any?): Int {
            if (o is Number) return o.toInt()
            return try {
                o?.toString()?.toInt() ?: 0
            } catch (_: Exception) {
                0
            }
        }

    }

    override fun isModeDisabled(modeId: GameModeId): Boolean = game.disabledModes.contains(modeId)

    override fun leaveDelaySeconds(): Int = game.leaveDelaySeconds

    override fun modeSection(modeId: GameModeId): ConfigurationSection? = modeSections[modeId]

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
                            sec.getStringList("disabled-modes"),
                            "game.disabled-modes",
                            GameModeId::parse
                        )
                    ),
                    leaveDelaySeconds = sec.getInt("leave-delay-seconds", 5)
                        .coerceAtLeast(0),
                    defaultMode = sec.getString("default-mode")
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let { raw ->
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
        @get:JvmName("failOnMigrationError") val failOnMigrationError: Boolean,
    ) {

        companion object {

            fun fromSection(sec: ConfigurationSection?): Database {
                if (sec == null) return defaults()

                val type = DatabaseType.parse(sec.getString("type", "sqlite"))
                val tablePrefix = sanitizeTablePrefix(sec.getString("table-prefix", "cia_"))
                val typed = sec.getConfigurationSection(type.name.lowercase(Locale.ROOT))

                val host = typed?.getString("host") ?: sec.getString("host", "localhost") ?: "localhost"

                val defaultPort = when (type) {
                    DatabaseType.MYSQL -> 3306
                    DatabaseType.POSTGRESQL -> 5432
                    DatabaseType.H2 -> 0
                    DatabaseType.SQLITE -> 0
                }
                val port = typed?.getInt("port", defaultPort)
                    ?: sec.getInt("port", defaultPort)

                val database = typed?.getString("database")
                    ?: sec.getString("database", "creepersiarena")
                    ?: "creepersiarena"
                val username = typed?.getString("username")
                    ?: sec.getString("username", "")
                    ?: ""
                val password = typed?.getString("password")
                    ?: sec.getString("password", "")
                    ?: ""
                val file = typed?.getString("file")
                    ?: sec.getString("file", defaultFile(type))
                    ?: defaultFile(type)
                val parameters = parseParameters(
                    typed?.getConfigurationSection("parameters")
                        ?: sec.getConfigurationSection("parameters")
                )

                val poolSize = positive(
                    typed?.getInt("pool-size", defaultPoolSize(type))
                        ?: sec.getInt("pool-size", defaultPoolSize(type)),
                    defaultPoolSize(type)
                )
                val connectionTimeoutMs = positiveLong(
                    typed?.getLong("connection-timeout-ms", 5000L)
                        ?: sec.getLong("connection-timeout-ms", 5000L),
                    5000L
                )
                val busyTimeoutMs = positive(
                    typed?.getInt("busy-timeout-ms", 5000)
                        ?: sec.getInt("busy-timeout-ms", 5000),
                    5000
                )
                val journalMode = (typed?.getString("journal-mode") ?: sec.getString("journal-mode", "WAL") ?: "WAL")
                    .trim().uppercase(Locale.ROOT)
                val synchronous =
                    (typed?.getString("synchronous") ?: sec.getString("synchronous", "NORMAL") ?: "NORMAL")
                        .trim().uppercase(Locale.ROOT)

                val executorSec = sec.getConfigurationSection("executor")
                val executorThreads = positive(executorSec?.getInt("threads", 2) ?: 2, 2)
                val executorQueueSize = positive(executorSec?.getInt("queue-size", 10000) ?: 10000, 10000)

                val migrationSec = sec.getConfigurationSection("migrations")
                val validateChecksum = migrationSec?.getBoolean("validate-checksum", true) ?: true
                val failOnError = migrationSec?.getBoolean("fail-on-error", true) ?: true

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
                    failOnError,
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
                failOnMigrationError = true,
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

            private fun positive(value: Int, fallback: Int): Int = if (value > 0) value else fallback

            private fun positiveLong(value: Long, fallback: Long): Long = if (value > 0L) value else fallback

            private fun parseParameters(sec: ConfigurationSection?): Map<String, String> {
                if (sec == null) return mapOf()

                val out = LinkedHashMap<String, String>()
                for (key in sec.getKeys(false)) {
                    val value = sec.get(key) ?: continue
                    out[key] = value.toString()
                }

                return Collections.unmodifiableMap(out)
            }

            private fun sanitizeTablePrefix(raw: String?): String {
                val text = raw?.trim() ?: "cia_"
                val cleaned = text.replace(Regex("[^A-Za-z0-9_]"), "_")
                if (cleaned.isBlank()) return "cia_"
                return cleaned
            }

        }

    }

    data class World(
        @get:JvmName("portalsEnabled") val portalsEnabled: Boolean,
    ) {

        companion object {

            fun fromSection(sec: ConfigurationSection?): World {
                if (sec == null) return defaults()
                return World(sec.getBoolean("portals-enabled", false))
            }

            fun defaults(): World = World(false)

        }

    }

}
