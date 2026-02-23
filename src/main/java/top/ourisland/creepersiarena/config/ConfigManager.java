package top.ourisland.creepersiarena.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.model.ArenaConfig;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.config.model.SkillConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConfigManager {
    private final JavaPlugin plugin;
    private final Logger logger;
    @Getter
    private final Path dataDir;

    @Getter
    private GlobalConfig globalConfig = GlobalConfig.defaults();
    @Getter
    private ArenaConfig arenaConfig = ArenaConfig.empty();
    @Getter
    private SkillConfig skillConfig = SkillConfig.defaults();

    public ConfigManager(JavaPlugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.dataDir = plugin.getDataFolder().toPath();
    }

    public void reloadAll() {
        ensureDataDir();
        copyDefaultIfAbsent("config.yml");
        copyDefaultIfAbsent("arena.yml");
        copyDefaultIfAbsent("skill.yml");

        this.globalConfig = loadGlobal();
        this.arenaConfig = loadArena();
        this.skillConfig = loadSkill();
    }

    public boolean setGlobalNode(String node, Object value) {
        return setNode("config.yml", node, value);
    }

    public boolean setSkillNode(String node, Object value) {
        return setNode("skill.yml", node, value);
    }

    private boolean setNode(String filename, String node, Object value) {
        ensureDataDir();
        copyDefaultIfAbsent(filename);

        Path p = dataDir.resolve(filename);
        try {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(p.toFile());
            yml.set(node, value);
            yml.save(p.toFile());
            return true;
        } catch (Throwable t) {
            logger.warn("[Config] Failed to write {} node={} value={}: {}", filename, node, value, t.getMessage(), t);
            return false;
        }
    }

    public boolean setArenaNode(String node, Object value) {
        return setNode("arena.yml", node, value);
    }

    public List<String> listGlobalKeys() {
        return listKeys("config.yml");
    }

    public List<String> listArenaKeys() {
        return listKeys("arena.yml");
    }

    public List<String> listSkillKeys() {
        return listKeys("skill.yml");
    }

    private List<String> listKeys(String filename) {
        ensureDataDir();
        copyDefaultIfAbsent(filename);

        Path p = dataDir.resolve(filename);
        try {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(p.toFile());
            List<String> out = new ArrayList<>();
            collectKeys(yml, "", out);
            Collections.sort(out);
            return out;
        } catch (Throwable t) {
            logger.warn("[Config] Failed to list keys for {}: {}", filename, t.getMessage(), t);
            return List.of();
        }
    }

    private void collectKeys(ConfigurationSection sec, String prefix, List<String> out) {
        if (sec == null) return;
        for (String k : sec.getKeys(false)) {
            String full = prefix.isEmpty() ? k : (prefix + "." + k);
            out.add(full);
            ConfigurationSection child = sec.getConfigurationSection(k);
            if (child != null) collectKeys(child, full, out);
        }
    }

    public Object getGlobalNode(String node) {
        return getNode("config.yml", node);
    }

    public Object getSkillNode(String node) {
        return getNode("skill.yml", node);
    }

    private Object getNode(String filename, String node) {
        ensureDataDir();
        copyDefaultIfAbsent(filename);

        Path p = dataDir.resolve(filename);
        try {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(p.toFile());
            return yml.get(node);
        } catch (Throwable t) {
            logger.warn("[Config] Failed to read {} node={}: {}", filename, node, t.getMessage(), t);
            return null;
        }
    }

    public Object getArenaNode(String node) {
        return getNode("arena.yml", node);
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


    private SkillConfig loadSkill() {
        Path p = dataDir.resolve("skill.yml");
        try {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(p.toFile());
            return SkillConfig.fromYaml(yml);
        } catch (Exception e) {
            logger.warn("[Config] Failed to load skill.yml: {}", e.getMessage(), e);
            return SkillConfig.defaults();
        }
    }

}
