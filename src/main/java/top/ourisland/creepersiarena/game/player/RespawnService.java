package top.ourisland.creepersiarena.game.player;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.util.Msg;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Respawn countdown ticker.
 *
 * <p>Important: this service ONLY manages countdown + callback.
 * It must NOT directly call transitions / change player stage. That is handled by {@code GameFlow}.</p>
 */
public final class RespawnService {

    private final Logger log;
    private final PlayerSessionStore store;

    private final Set<UUID> ticking = new HashSet<>();
    private Consumer<Player> callback;

    public RespawnService(
            @lombok.NonNull Logger log,
            @lombok.NonNull PlayerSessionStore store
    ) {
        this.log = log;
        this.store = store;
    }

    public void callback(Consumer<Player> callback) {
        this.callback = callback;
    }

    public void startOrReset(Player p, int seconds) {
        if (p == null) return;

        PlayerSession s = store.get(p);
        if (s == null) return;

        int sec = Math.max(0, seconds);
        s.respawnSecondsRemaining(sec);

        if (sec == 0) {
            ticking.remove(p.getUniqueId());
            if (callback != null) callback.accept(p);
            return;
        }

        ticking.add(p.getUniqueId());
        log.debug("[Respawn] startOrReset: name={} sec={}", p.getName(), sec);
    }

    public void cancel(Player p) {
        if (p == null) return;

        ticking.remove(p.getUniqueId());

        PlayerSession s = store.get(p);
        if (s != null) {
            s.respawnSecondsRemaining(0);
        }
    }

    public void cancelAll() {
        ticking.clear();
    }

    /**
     * Called by the global 1-second ticker (see GameTickModule -> GameFlow.tick1s()).
     */
    public void tick1s() {
        if (ticking.isEmpty()) return;

        // Copy to avoid CME when callback changes state or cancels.
        UUID[] uuids = ticking.toArray(UUID[]::new);

        for (UUID uuid : uuids) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                ticking.remove(uuid);
                continue;
            }

            PlayerSession s = store.get(p);
            if (s == null) {
                ticking.remove(uuid);
                continue;
            }

            if (s.state() != PlayerState.RESPAWN) {
                ticking.remove(uuid);
                continue;
            }

            int remain = s.respawnSecondsRemaining();

            if (remain <= 1) {
                s.respawnSecondsRemaining(0);
                ticking.remove(uuid);
                if (callback != null) {
                    try {
                        callback.accept(p);
                    } catch (Throwable t) {
                        log.warn("[Respawn] callback failed: {}", t.getMessage(), t);
                    }
                }
                continue;
            }

            Msg.actionBar(p, Component.text("复活倒计时: " + remain + "s"));
            s.respawnSecondsRemaining(remain - 1);
        }
    }
}
