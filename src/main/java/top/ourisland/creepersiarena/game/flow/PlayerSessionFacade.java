package top.ourisland.creepersiarena.game.flow;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemService;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.job.JobId;

import java.util.Optional;

/**
 * Internal helper that owns:
 * - PlayerSession creation/lookup
 * - Selected job persistence (PDC + session) + default job fallback
 *
 * <p>Package-private: only used inside {@link top.ourisland.creepersiarena.game.flow.GameFlow}.</p>
 */
final class PlayerSessionFacade {

    private final Logger log;
    private final PlayerSessionStore store;
    private final LobbyItemService lobbyItemService;

    private final NamespacedKey selectedJobKey;

    PlayerSessionFacade(
            @lombok.NonNull Plugin plugin,
            @lombok.NonNull Logger log,
            @lombok.NonNull PlayerSessionStore store,
            @lombok.NonNull LobbyItemService lobbyItemService
    ) {
        this.log = log;
        this.store = store;
        this.lobbyItemService = lobbyItemService;

        this.selectedJobKey = new NamespacedKey(plugin, "selected_job");
    }

    PlayerSession get(Player p) {
        return store.get(p);
    }

    PlayerSession getOrCreate(Player p) {
        return store.getOrCreate(p);
    }

    void remove(Player p) {
        store.remove(p);
    }

    PlayerSession ensureSession(Player p) {
        PlayerSession s = store.getOrCreate(p);
        ensureSelectedJob(p, s);
        return s;
    }

    void persistSelectedJob(Player p, JobId jobId) {
        if (jobId == null) return;
        p.getPersistentDataContainer().set(selectedJobKey, PersistentDataType.STRING, jobId.toString());
    }

    void ensureSelectedJob(Player p, PlayerSession s) {
        Optional.ofNullable(s.selectedJob())

                .or(() -> Optional.ofNullable(
                                p.getPersistentDataContainer().get(selectedJobKey, PersistentDataType.STRING)
                        ).filter(lobbyItemService::hasJobId)
                        .map(JobId::fromId))

                .or(() -> Optional.ofNullable(
                                lobbyItemService.firstJobIdOrNull()
                        ).filter(lobbyItemService::hasJobId)
                        .map(JobId::fromId)
                        .map(jid -> {
                            persistSelectedJob(p, jid);
                            return jid;
                        }))

                .ifPresentOrElse(
                        s::selectedJob,
                        () -> log.warn("[Lobby] No available job to select for {}", p.getName())
                );
    }
}
