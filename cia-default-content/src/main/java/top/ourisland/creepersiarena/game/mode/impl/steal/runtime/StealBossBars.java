package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class StealBossBars {

    private final BossBar waiting = BossBar.bossBar(
            Component.empty(),
            0.0f,
            BossBar.Color.RED,
            BossBar.Overlay.PROGRESS
    );
    private final BossBar spectator = BossBar.bossBar(
            Component.text("观察地图", NamedTextColor.GRAY),
            1.0f,
            BossBar.Color.WHITE,
            BossBar.Overlay.PROGRESS
    );
    private final BossBar chooseJob = BossBar.bossBar(
            Component.text("选择职业", NamedTextColor.AQUA),
            1.0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.PROGRESS
    );
    private final BossBar round = BossBar.bossBar(
            Component.empty(),
            1.0f,
            BossBar.Color.GREEN,
            BossBar.Overlay.PROGRESS
    );
    private final BossBar celebration = BossBar.bossBar(
            Component.text("庆祝时刻", NamedTextColor.GOLD),
            1.0f,
            BossBar.Color.YELLOW,
            BossBar.Overlay.PROGRESS
    );

    private final Map<BossBar, Set<UUID>> viewers = new IdentityHashMap<>();

    void showWaiting(
            Collection<Player> players,
            int ready,
            int needed,
            int population,
            int countdownRemaining,
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

    private void sync(BossBar bar, @NonNull Collection<Player> players) {
        Set<UUID> desired = players.stream()
                .filter(p -> p != null && p.isOnline())
                .map(Entity::getUniqueId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<UUID> current = viewers.computeIfAbsent(bar, _ -> new LinkedHashSet<>());
        Arrays.stream(current.toArray(UUID[]::new))
                .filter(id -> !desired.contains(id))
                .forEach(id -> {
                    var old = Bukkit.getPlayer(id);
                    if (old != null) old.hideBossBar(bar);
                    current.remove(id);
                });

        players.stream()
                .filter(p -> p != null && p.isOnline())
                .filter(p -> current.add(p.getUniqueId()))
                .forEach(p -> p.showBossBar(bar));
    }

    private void hideAllExcept(BossBar keep, Collection<Player> visiblePlayers) {
        Stream.of(waiting, spectator, chooseJob, round, celebration)
                .filter(bar -> bar != keep)
                .forEach(bar -> hide(bar, visiblePlayers));
    }

    private void hide(BossBar bar, Collection<Player> visiblePlayers) {
        Set<UUID> current = viewers.computeIfAbsent(bar, _ -> new LinkedHashSet<>());
        Set<UUID> visible = visiblePlayers.stream()
                .filter(Objects::nonNull)
                .map(Entity::getUniqueId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Arrays.stream(current.toArray(UUID[]::new))
                .forEach(id -> {
                    var player = Bukkit.getPlayer(id);
                    if (player != null) player.hideBossBar(bar);
                    current.remove(id);
                });

        visible.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> player.hideBossBar(bar));
    }

    void showSpectator(
            Collection<Player> players,
            int remaining,
            int max
    ) {
        spectator.progress(ratio(remaining, max));
        sync(spectator, players);
        hideAllExcept(spectator, players);
    }

    void showChooseJob(
            Collection<Player> players,
            int remaining,
            int max
    ) {
        chooseJob.progress(ratio(remaining, max));
        sync(chooseJob, players);
        hideAllExcept(chooseJob, players);
    }

    void showRound(
            Collection<Player> players,
            int remaining,
            int max,
            int mined,
            int target
    ) {
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

    void showCelebration(
            Collection<Player> players,
            int remaining,
            int max,
            boolean finalGame
    ) {
        celebration.name(Component.text(finalGame ? "最终庆祝" : "庆祝时刻", NamedTextColor.GOLD));
        celebration.progress(ratio(remaining, max));
        sync(celebration, players);
        hideAllExcept(celebration, players);
    }

    void hideWaiting(Collection<Player> visiblePlayers) {
        hide(waiting, visiblePlayers);
    }

    void hideSpectator(Collection<Player> visiblePlayers) {
        hide(spectator, visiblePlayers);
    }

    void hideChooseJob(Collection<Player> visiblePlayers) {
        hide(chooseJob, visiblePlayers);
    }

    void hideRound(Collection<Player> visiblePlayers) {
        hide(round, visiblePlayers);
    }

    void hideCelebration(Collection<Player> visiblePlayers) {
        hide(celebration, visiblePlayers);
    }

    void hideAllTracked() {
        var online = allTrackedViewers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        hideAll(online);
    }

    private Set<UUID> allTrackedViewers() {
        return viewers.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    void hideAll(Collection<Player> visiblePlayers) {
        List.of(waiting, spectator, chooseJob, round, celebration)
                .forEach(bar -> hide(bar, visiblePlayers));
    }

}
