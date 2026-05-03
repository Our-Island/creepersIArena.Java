package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

final class StealBossBars {

    private final BossBar waiting = BossBar.bossBar(Component.empty(), 0.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
    private final BossBar spectator = BossBar.bossBar(Component.text("观察地图", NamedTextColor.GRAY), 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
    private final BossBar chooseJob = BossBar.bossBar(Component.text("选择职业", NamedTextColor.AQUA), 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
    private final BossBar round = BossBar.bossBar(Component.empty(), 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
    private final BossBar celebration = BossBar.bossBar(Component.text("庆祝时刻", NamedTextColor.GOLD), 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);

    private final Map<BossBar, Set<UUID>> viewers = new IdentityHashMap<>();

    void showWaiting(
            Collection<Player> players, int ready, int needed, int population, int countdownRemaining,
            int countdownMax
    ) {
        waiting.name(waitingName(ready, needed, countdownRemaining));
        waiting.progress(ratio(countdownRemaining > 0 ? countdownRemaining : ready, countdownRemaining > 0
                ? countdownMax
                : Math.max(1, needed)));
        waiting.color(countdownRemaining > 0 ? BossBar.Color.GREEN : BossBar.Color.RED);
        sync(waiting, players);
        hideAllExcept(waiting, players);
    }

    private Component waitingName(int ready, int needed, int countdownRemaining) {
        var base = Component.text("已准备 ", NamedTextColor.WHITE)
                .append(Component.text(ready, NamedTextColor.GREEN))
                .append(Component.text(" / 需要 ", NamedTextColor.WHITE))
                .append(Component.text(needed, countdownRemaining > 0 ? NamedTextColor.GREEN : NamedTextColor.RED));
        if (countdownRemaining > 0) {
            return base.append(Component.text("  开始倒计时 ", NamedTextColor.WHITE))
                    .append(Component.text(countdownRemaining + "s", NamedTextColor.YELLOW));
        }
        return base;
    }

    private float ratio(int value, int max) {
        if (max <= 0) return 0.0f;
        return Math.clamp((float) value / (float) max, 0.0f, 1.0f);
    }

    private void sync(BossBar bar, Collection<Player> players) {
        Set<UUID> desired = new LinkedHashSet<>();
        for (var p : players) {
            if (p == null || !p.isOnline()) continue;
            desired.add(p.getUniqueId());
        }

        Set<UUID> current = viewers.computeIfAbsent(bar, ignored -> new LinkedHashSet<>());
        for (UUID id : current.toArray(UUID[]::new)) {
            if (desired.contains(id)) continue;
            Player old = Bukkit.getPlayer(id);
            if (old != null) old.hideBossBar(bar);
            current.remove(id);
        }

        for (Player p : players) {
            if (p == null || !p.isOnline()) continue;
            p.showBossBar(bar);
            current.add(p.getUniqueId());
        }
    }

    private void hideAllExcept(BossBar keep, Collection<Player> visiblePlayers) {
        for (var bar : List.of(waiting, spectator, chooseJob, round, celebration)) {
            if (bar != keep) hide(bar, visiblePlayers);
        }
    }

    private void hide(BossBar bar, Collection<Player> visiblePlayers) {
        Set<UUID> current = viewers.computeIfAbsent(bar, ignored -> new LinkedHashSet<>());
        Set<UUID> visible = new LinkedHashSet<>();
        for (var p : visiblePlayers) {
            if (p != null) visible.add(p.getUniqueId());
        }

        for (UUID id : current.toArray(UUID[]::new)) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) player.hideBossBar(bar);
            current.remove(id);
        }

        for (UUID id : visible) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) player.hideBossBar(bar);
        }
    }

    void showSpectator(Collection<Player> players, int remaining, int max) {
        spectator.progress(ratio(remaining, max));
        sync(spectator, players);
        hideAllExcept(spectator, players);
    }

    void showChooseJob(Collection<Player> players, int remaining, int max) {
        chooseJob.progress(ratio(remaining, max));
        sync(chooseJob, players);
        hideAllExcept(chooseJob, players);
    }

    void showRound(Collection<Player> players, int remaining, int max, int mined, int target) {
        round.name(Component.text("已有 ", NamedTextColor.WHITE)
                .append(Component.text(mined, NamedTextColor.RED))
                .append(Component.text(" / ", NamedTextColor.WHITE))
                .append(Component.text(target, NamedTextColor.RED))
                .append(Component.text(" 块红石矿被拆除  ", NamedTextColor.WHITE))
                .append(Component.text(remaining + "s", NamedTextColor.YELLOW)));
        round.progress(ratio(remaining, max));
        round.color(remaining <= Math.max(10, max / 4) ? BossBar.Color.RED : BossBar.Color.GREEN);
        sync(round, players);
        hideAllExcept(round, players);
    }

    void showCelebration(Collection<Player> players, int remaining, int max, boolean finalGame) {
        celebration.name(Component.text(finalGame ? "最终庆祝" : "庆祝时刻", NamedTextColor.GOLD));
        celebration.progress(ratio(remaining, max));
        sync(celebration, players);
        hideAllExcept(celebration, players);
    }

    void hideAllTracked() {
        var online = new ArrayList<Player>();
        for (UUID id : allTrackedViewers()) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) online.add(player);
        }
        hideAll(online);
    }

    private Set<UUID> allTrackedViewers() {
        var out = new LinkedHashSet<UUID>();
        for (Set<UUID> ids : viewers.values()) out.addAll(ids);
        return out;
    }

    void hideAll(Collection<Player> visiblePlayers) {
        for (var bar : List.of(waiting, spectator, chooseJob, round, celebration)) {
            hide(bar, visiblePlayers);
        }
    }

}
