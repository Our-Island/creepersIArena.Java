package top.ourisland.creepersiarena.game.playerdata;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataService {

    private final Plugin plugin;
    private final Logger logger;
    private final Path folder;
    private final Map<UUID, PlayerDataDocument> documents = new ConcurrentHashMap<>();

    public PlayerDataService(
            Plugin plugin,
            Logger logger,
            Path dataDir
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.folder = dataDir.resolve("player-data");
        ensureFolder();
    }

    private void ensureFolder() {
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            logger.warn("[PlayerData] Failed to create {}: {}", folder, e.getMessage(), e);
        }
    }

    public void loadAsync(UUID playerId) {
        if (playerId == null || loaded(playerId)) return;
        Bukkit.getAsyncScheduler().runNow(plugin, _ -> {
            PlayerDataDocument doc = loadNow(playerId);
            documents.put(playerId, doc);
        });
    }

    public boolean loaded(UUID playerId) {
        return playerId != null && documents.containsKey(playerId);
    }

    private PlayerDataDocument loadNow(UUID playerId) {
        ensureFolder();
        var path = path(playerId);
        try {
            if (!Files.exists(path)) return new PlayerDataDocument(new YamlConfiguration());
            return new PlayerDataDocument(YamlConfiguration.loadConfiguration(path.toFile()));
        } catch (Throwable t) {
            logger.warn("[PlayerData] Failed to load {}: {}", playerId, t.getMessage(), t);
            return new PlayerDataDocument(new YamlConfiguration());
        }
    }

    private Path path(UUID playerId) {
        return folder.resolve(playerId + ".yml");
    }

    public PlayerDataDocument document(UUID playerId) {
        if (playerId == null) throw new IllegalArgumentException("playerId is null");
        return documents.computeIfAbsent(playerId, this::loadNow);
    }

    public void saveAndUnloadAsync(UUID playerId) {
        if (playerId == null) return;
        PlayerDataDocument doc = documents.remove(playerId);
        if (doc == null || !doc.dirty()) return;
        String content = doc.saveToStringAndClearDirty();
        var path = path(playerId);
        Bukkit.getAsyncScheduler().runNow(plugin, _ -> write(path, content));
    }

    private void write(
            Path path,
            String content
    ) {
        ensureFolder();
        try {
            Files.writeString(path, content == null ? "" : content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("[PlayerData] Failed to save {}: {}", path.getFileName(), e.getMessage(), e);
        }
    }

    public void flushDirtyAsync() {
        documents.keySet().forEach(this::saveAsync);
    }

    public void saveAsync(UUID playerId) {
        if (playerId == null) return;
        PlayerDataDocument doc = documents.get(playerId);
        if (doc == null || !doc.dirty()) return;
        String content = doc.saveToStringAndClearDirty();
        var path = path(playerId);
        Bukkit.getAsyncScheduler().runNow(plugin, _ -> write(path, content));
    }

    public void flushAllBlocking() {
        documents.forEach((key, doc) -> {
            if (doc == null || !doc.dirty()) return;
            write(path(key), doc.saveToStringAndClearDirty());
        });
    }

}
