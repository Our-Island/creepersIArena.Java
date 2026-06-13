package top.ourisland.creepersiarena.defaultcontent.game.mode.battle;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class BattleBossBars {

    private final Set<UUID> viewers = new HashSet<>();
    private BossBar mapProgress;

    public void update(BattleState state) {
        if (state == null) return;

        var title = Component.text("Battle", NamedTextColor.RED)
                .append(separator())
                .append(Component.text(state.session().arena().id().value(), NamedTextColor.YELLOW))
                .append(separator())
                .append(Component.text(state.mapProgress(), NamedTextColor.WHITE))
                .append(Component.text("/", NamedTextColor.GRAY))
                .append(Component.text(state.config().mapProgressTarget(), NamedTextColor.WHITE))
                .append(separator())
                .append(state.scoreSummaryComponent());
        float progress = state.progressRatio();

        if (mapProgress == null) {
            mapProgress = BossBar.bossBar(title, progress, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        } else {
            mapProgress.name(title);
            mapProgress.progress(progress);
        }

        Set<UUID> desired = new HashSet<>();
        for (var uuid : state.players()) {
            var player = Bukkit.getPlayer(uuid);
            if (state.isFighter(player)) {
                desired.add(uuid);
                player.showBossBar(mapProgress);
            }
        }

        for (var uuid : Set.copyOf(viewers)) {
            if (desired.contains(uuid)) continue;
            var player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline() && mapProgress != null) {
                player.hideBossBar(mapProgress);
            }
            viewers.remove(uuid);
        }
        viewers.addAll(desired);
    }

    private Component separator() {
        return Component.text(" | ", NamedTextColor.DARK_GRAY);
    }

    public void hide() {
        if (mapProgress == null) return;
        Set.copyOf(viewers).stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .forEach(player -> player.hideBossBar(mapProgress));
        viewers.clear();
        mapProgress = null;
    }

}
