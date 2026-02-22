package top.ourisland.creepersiarena.game.flow;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.game.lobby.inventory.InventorySnapshot;
import top.ourisland.creepersiarena.game.lobby.inventory.LobbyItemService;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.job.JobId;

import java.util.Optional;

/**
 * Internal helper that owns:
 * - PlayerSession creation/lookup
 * - Outside inventory snapshot persistence (PDC + session)
 * - Selected job persistence (PDC + session) + default job fallback
 *
 * <p>Package-private: only used inside {@link top.ourisland.creepersiarena.game.flow.GameFlow}.</p>
 */
final class PlayerSessionFacade {

    private final Logger log;
    private final PlayerSessionStore store;
    private final LobbyItemService lobbyItemService;

    private final NamespacedKey selectedJobKey;
    private final NamespacedKey outsideSnapshotKey;

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
        this.outsideSnapshotKey = new NamespacedKey(plugin, "outside_snapshot");
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

        ensureOutsideSnapshot(p, s);
        ensureSelectedJob(p, s);

        return s;
    }

    void ensureOutsideSnapshot(Player p, PlayerSession s) {
        if (s.outsideSnapshot() != null) return;

        InventorySnapshot snap = loadOutsideSnapshotFromPdc(p);
        if (snap == null) {
            snap = InventorySnapshot.capture(p);
            saveOutsideSnapshotToPdc(p, snap);
            log.debug("[Session] outside snapshot captured & persisted: {}", p.getName());
        } else {
            log.debug("[Session] outside snapshot loaded from PDC: {}", p.getName());
        }

        s.outsideSnapshot(snap);
    }

    InventorySnapshot loadOutsideSnapshotFromPdc(Player p) {
        try {
            byte[] bytes = p.getPersistentDataContainer().get(outsideSnapshotKey, PersistentDataType.BYTE_ARRAY);
            if (bytes == null || bytes.length == 0) return null;
            return InventorySnapshot.decode(bytes);
        } catch (Throwable t) {
            log.warn("[Session] failed to decode outside snapshot for {}: {}", p.getName(), t.getMessage());
            return null;
        }
    }

    void saveOutsideSnapshotToPdc(Player p, InventorySnapshot snap) {
        if (snap == null) return;
        try {
            byte[] bytes = snap.encode();
            p.getPersistentDataContainer().set(outsideSnapshotKey, PersistentDataType.BYTE_ARRAY, bytes);
        } catch (Throwable t) {
            log.warn("[Session] failed to encode outside snapshot for {}: {}", p.getName(), t.getMessage());
        }
    }

    void clearOutsideSnapshotFromPdc(Player p) {
        try {
            p.getPersistentDataContainer().remove(outsideSnapshotKey);
        } catch (Throwable ignored) {
        }
    }

    void persistSelectedJob(Player p, JobId jobId) {
        if (jobId == null) return;
        p.getPersistentDataContainer().set(selectedJobKey, PersistentDataType.STRING, jobId.toString());
    }

    void clearSelectedJobFromPdc(Player p) {
        try {
            p.getPersistentDataContainer().remove(selectedJobKey);
        } catch (Throwable ignored) {
        }
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

    /**
     * Restore player's "outside" snapshot if possible, then clear stored PDC keys.
     * This is used when leaving CIA completely (leave-to-outside or quitting server).
     */
    void restoreOutsideSnapshotAndClear(Player p) {
        PlayerSession s = store.get(p);

        InventorySnapshot snap = (s != null) ? s.outsideSnapshot() : null;
        if (snap == null) {
            snap = loadOutsideSnapshotFromPdc(p);
        }

        if (snap != null) {
            try {
                snap.restore(p);
            } catch (Throwable t) {
                log.warn("[Session] failed to restore outside snapshot for {}: {}", p.getName(), t.getMessage());
            }
        }

        clearOutsideSnapshotFromPdc(p);
        clearSelectedJobFromPdc(p);
    }
}
