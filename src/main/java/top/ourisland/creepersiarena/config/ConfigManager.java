package top.ourisland.creepersiarena.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.model.ArenaConfig;
import top.ourisland.creepersiarena.config.model.GlobalConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final Path dataDir;

    private GlobalConfig globalConfig = GlobalConfig.defaults();
    private ArenaConfig arenaConfig = ArenaConfig.empty();

    public ConfigManager(JavaPlugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.dataDir = plugin.getDataFolder().toPath();
    }

    public void reloadAll() {
        ensureDataDir();
        copyDefaultIfAbsent("config.yml");
        copyDefaultIfAbsent("arena.yml");

        this.globalConfig = loadGlobal();
        this.arenaConfig = loadArena();
    }

    private void ensureDataDir() {
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            logger.error("[Config] Cannot create plugin data directory: {}", dataDir, e);
        }
    }

    private void copyDefaultIfAbsent(String filename) {
        Path dest = dataDir.resolve(filename);
        if (Files.exists(dest)) return;

        try (InputStream in = plugin.getResource(filename)) {
            if (in == null) {
                logger.warn("[Config] Default resource {} not found in jar, creating empty file.", filename);
                Files.writeString(dest, "# " + filename + "\n");
                return;
            }
            Files.copy(in, dest);
            logger.info("[Config] Generated default {}", dest.getFileName());
        } catch (IOException e) {
            logger.error("[Config] Failed to generate default {}", filename, e);
        }
    }

    private GlobalConfig loadGlobal() {
        logger.info("[Config] Loading global config...");
        Path p = dataDir.resolve("config.yml");
        try {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(p.toFile());
            return GlobalConfig.fromYaml(yml);
        } catch (Exception e) {
            logger.error("[Config] Failed to load config.yml, using defaults.", e);
            return GlobalConfig.defaults();
        }
    }

    private ArenaConfig loadArena() {
        logger.info("[Config] Loading arena config...");
        Path p = dataDir.resolve("arena.yml");
        try {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(p.toFile());
            return ArenaConfig.fromYaml(yml);
        } catch (Exception e) {
            logger.error("[Config] Failed to load arena.yml, using empty config.", e);
            return ArenaConfig.empty();
        }
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public ArenaConfig getArenaConfig() {
        return arenaConfig;
    }

    public Path getDataDir() {
        return dataDir;
    }
}
