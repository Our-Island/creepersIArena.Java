package top.ourisland.creepersiarena.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import top.ourisland.creepersiarena.config.model.ArenaConfig
import top.ourisland.creepersiarena.config.model.GlobalConfig
import top.ourisland.creepersiarena.config.model.SkillConfig
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.jvm.JvmName

/**
 * Loads and writes plugin configurations.
 *
 * Notes:
 * - Keeps the fluent getter style used by Lombok in the original Java implementation,
 *   so Java callers can continue using `cfg.globalConfig()` / `cfg.skillConfig()` etc.
 * - Still reads/writes Bukkit [YamlConfiguration].
 */
class ConfigManager(
    private val plugin: JavaPlugin,
    private val logger: Logger,
) {

    @get:JvmName("dataDir")
    val dataDir: Path = plugin.dataFolder.toPath()

    @get:JvmName("globalConfig")
    var globalConfig: GlobalConfig = GlobalConfig.defaults()
        private set

    @get:JvmName("arenaConfig")
    var arenaConfig: ArenaConfig = ArenaConfig.empty()
        private set

    @get:JvmName("skillConfig")
    var skillConfig: SkillConfig = SkillConfig.defaults()
        private set

    fun reloadAll() {
        ensureDataDir()
        copyDefaultIfAbsent("config.yml")
        copyDefaultIfAbsent("arena.yml")
        copyDefaultIfAbsent("skill.yml")

        globalConfig = loadGlobal()
        arenaConfig = loadArena()
        skillConfig = loadSkill()
    }

    fun setGlobalNode(node: String, value: Any?): Boolean = setNode("config.yml", node, value)

    fun setArenaNode(node: String, value: Any?): Boolean = setNode("arena.yml", node, value)

    fun setSkillNode(node: String, value: Any?): Boolean = setNode("skill.yml", node, value)

    private fun setNode(filename: String, node: String, value: Any?): Boolean {
        ensureDataDir()
        copyDefaultIfAbsent(filename)

        val p = dataDir.resolve(filename)
        return try {
            val yml = YamlConfiguration.loadConfiguration(p.toFile())
            yml.set(node, value)
            yml.save(p.toFile())
            true
        } catch (t: Throwable) {
            logger.warn("[Config] Failed to write {} node={} value={}: {}", filename, node, value, t.message, t)
            false
        }
    }

    fun listGlobalKeys(): List<String> = listKeys("config.yml")

    fun listArenaKeys(): List<String> = listKeys("arena.yml")

    fun listSkillKeys(): List<String> = listKeys("skill.yml")

    private fun listKeys(filename: String): List<String> {
        ensureDataDir()
        copyDefaultIfAbsent(filename)

        val p = dataDir.resolve(filename)
        return try {
            val yml = YamlConfiguration.loadConfiguration(p.toFile())
            val out = ArrayList<String>()
            collectKeys(yml, "", out)
            out.sort()
            out
        } catch (t: Throwable) {
            logger.warn("[Config] Failed to list keys for {}: {}", filename, t.message, t)
            listOf()
        }
    }

    private fun collectKeys(sec: ConfigurationSection?, prefix: String, out: MutableList<String>) {
        if (sec == null) return
        for (k in sec.getKeys(false)) {
            val full = if (prefix.isEmpty()) k else "$prefix.$k"
            out.add(full)
            collectKeys(sec.getConfigurationSection(k), full, out)
        }
    }

    fun getGlobalNode(node: String): Any? = getNode("config.yml", node)

    fun getArenaNode(node: String): Any? = getNode("arena.yml", node)

    fun getSkillNode(node: String): Any? = getNode("skill.yml", node)

    private fun getNode(filename: String, node: String): Any? {
        ensureDataDir()
        copyDefaultIfAbsent(filename)

        val p = dataDir.resolve(filename)
        return try {
            val yml = YamlConfiguration.loadConfiguration(p.toFile())
            yml.get(node)
        } catch (t: Throwable) {
            logger.warn("[Config] Failed to read {} node={}: {}", filename, node, t.message, t)
            null
        }
    }

    private fun ensureDataDir() {
        try {
            Files.createDirectories(dataDir)
        } catch (e: IOException) {
            logger.error("[Config] Cannot create plugin data directory: {}", dataDir, e)
        }
    }

    private fun copyDefaultIfAbsent(filename: String) {
        val dest = dataDir.resolve(filename)
        if (Files.exists(dest)) return

        try {
            plugin.getResource(filename).use { input: InputStream? ->
                if (input == null) {
                    logger.warn("[Config] Default resource {} not found in jar, creating empty file.", filename)
                    Files.writeString(dest, "# $filename\n")
                    return
                }
                Files.copy(input, dest)
                logger.info("[Config] Generated default {}", dest.fileName)
            }
        } catch (e: IOException) {
            logger.error("[Config] Failed to generate default {}", filename, e)
        }
    }

    private fun loadGlobal(): GlobalConfig {
        logger.info("[Config] Loading global config...")
        val p = dataDir.resolve("config.yml")
        return try {
            val yml = YamlConfiguration.loadConfiguration(p.toFile())
            GlobalConfig.fromYaml(yml)
        } catch (e: Exception) {
            logger.error("[Config] Failed to load config.yml, using defaults.", e)
            GlobalConfig.defaults()
        }
    }

    private fun loadArena(): ArenaConfig {
        logger.info("[Config] Loading arena config...")
        val p = dataDir.resolve("arena.yml")
        return try {
            val yml = YamlConfiguration.loadConfiguration(p.toFile())
            ArenaConfig.fromYaml(yml)
        } catch (e: Exception) {
            logger.error("[Config] Failed to load arena.yml, using empty config.", e)
            ArenaConfig.empty()
        }
    }

    private fun loadSkill(): SkillConfig {
        val p = dataDir.resolve("skill.yml")
        return try {
            val yml = YamlConfiguration.loadConfiguration(p.toFile())
            SkillConfig.fromYaml(yml)
        } catch (e: Exception) {
            logger.warn("[Config] Failed to load skill.yml: {}", e.message, e)
            SkillConfig.defaults()
        }
    }
}
