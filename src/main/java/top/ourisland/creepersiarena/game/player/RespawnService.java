package top.ourisland.creepersiarena.game.player;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.game.flow.PlayerTransitions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class RespawnService {

    private final Plugin plugin;
    private final Logger log;
    private final PlayerSessionStore store;
    private final PlayerTransitions transitions;

    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    private Consumer<Player> callback;

    public RespawnService(Plugin plugin, Logger log, PlayerSessionStore store, PlayerTransitions transitions) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.log = Objects.requireNonNull(log, "log");
        this.store = Objects.requireNonNull(store, "store");
        this.transitions = Objects.requireNonNull(transitions, "transitions");
    }

    /**
     * 让倒计时结束后回到 Flow 统一处理（比如：STEAL 死亡直接观战、BATTLE 回战场等）。 如果未设置 callback，则使用默认行为：直接回战场出生点。
     */
    public void setCallback(Consumer<Player> callback) {
        this.callback = callback;
    }

    /**
     * 以配置的默认复活时间启动倒计时。
     */
    public void start(Player p) {
        PlayerSession s = store.getOrCreate(p);
        if (s.respawnSecondsRemaining() <= 0) {
            s.respawnSecondsRemaining(transitions.battleRespawnSecondsConfigured());
        }
        startInternal(p);
    }

    private void startInternal(Player p) {
        UUID id = p.getUniqueId();

        cancel(p);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!p.isOnline()) {
                cancel(p);
                return;
            }

            PlayerSession s = store.get(p);
            if (s == null) {
                cancel(p);
                return;
            }

            int remain = s.respawnSecondsRemaining();
            if (remain <= 0) {
                cancel(p);

                Consumer<Player> cb = this.callback;
                if (cb != null) {
                    try {
                        cb.accept(p);
                    } catch (Throwable t) {
                        log.warn("[Respawn] callback failed for {}", p.getName(), t);
                    }
                } else {
                    transitions.toBattle(p);
                }
                return;
            }

            p.sendActionBar(Component.text("复活倒计时： " + remain + " 秒"));
            s.respawnSecondsRemaining(remain - 1);
        }, 0L, 20L);

        tasks.put(id, task);
        log.debug("[Respawn] {} respawn task started ({}s)", p.getName(), store.getOrCreate(p).respawnSecondsRemaining());
    }

    public void cancel(Player p) {
        BukkitTask task = tasks.remove(p.getUniqueId());
        if (task != null) task.cancel();
    }

    /**
     * 以指定秒数启动倒计时（由 Flow/模式决定）。
     */
    public void start(Player p, int seconds) {
        PlayerSession s = store.getOrCreate(p);
        s.respawnSecondsRemaining(Math.max(0, seconds));
        startInternal(p);
    }
}
