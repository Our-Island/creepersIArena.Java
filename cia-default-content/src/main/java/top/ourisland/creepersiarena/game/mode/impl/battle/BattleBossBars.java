package top.ourisland.creepersiarena.game.mode.impl.battle;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class BattleBossBars {

    private final Set<UUID> viewers = new HashSet<>();
    private BossBar mapProgress;

    public void update(BattleState state) {
        if (state == null) return;

        Component title = Component.text("Battle", NamedTextColor.RED)
                .append(separator())
                .append(Component.text(state.session().arena().id(), NamedTextColor.YELLOW))
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
        for (UUID uuid : state.players()) {
            Player player = Bukkit.getPlayer(uuid);
            if (state.isFighter(player)) {
                desired.add(uuid);
                player.showBossBar(mapProgress);
            }
        }

        for (UUID uuid : Set.copyOf(viewers)) {
            if (desired.contains(uuid)) continue;
            Player player = Bukkit.getPlayer(uuid);
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
        for (UUID uuid : Set.copyOf(viewers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.hideBossBar(mapProgress);
            }
        }
        viewers.clear();
        mapProgress = null;
    }

}
