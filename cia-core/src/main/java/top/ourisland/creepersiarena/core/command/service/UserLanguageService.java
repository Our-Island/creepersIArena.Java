package top.ourisland.creepersiarena.core.command.service;

import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.database.PlayerRepository;
import top.ourisland.creepersiarena.core.player.PlayerDataParticipant;
import top.ourisland.creepersiarena.core.player.PlayerDataService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UserLanguageService implements PlayerDataParticipant {

    private final Logger logger;
    private final JdbcDatabaseService database;
    private final PlayerRepository players;
    private final Map<UUID, String> languageByPlayer = new ConcurrentHashMap<>();

    public UserLanguageService(
            Logger logger,
            JdbcDatabaseService database,
            PlayerDataService playerData
    ) {
        this.logger = logger;
        this.database = database;
        this.players = new PlayerRepository(database);
        playerData.registerParticipant(this);
    }

    @Override
    public void load(UUID playerId) throws Exception {
        String language = players.language(playerId);
        if (language == null) {
            languageByPlayer.remove(playerId);
        } else {
            languageByPlayer.put(playerId, language);
        }
    }

    @Override
    public void unload(UUID playerId) {
        languageByPlayer.remove(playerId);
    }

    public String getOrNull(Player p) {
        if (p == null) return null;
        String v = languageByPlayer.get(p.getUniqueId());
        if (v == null || v.isBlank()) return null;
        return v.trim();
    }

    public void set(Player p, String langOrNull) {
        if (p == null) return;
        var playerId = p.getUniqueId();
        String value = langOrNull == null || langOrNull.isBlank() ? null : langOrNull.trim();
        if (value == null) {
            languageByPlayer.remove(playerId);
        } else {
            languageByPlayer.put(playerId, value);
        }

        database.runAsync(() -> players.setLanguage(playerId, value)).exceptionally(error -> {
            logger.warn("[PlayerProfile] Failed to persist language for {}: {}", playerId, error.getMessage(), error);
            return null;
        });
    }

}
