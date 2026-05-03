package top.ourisland.creepersiarena.game.mode.impl.battle;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
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

        Component title = Component.text("§cBattle §8| §e" + state.session().arena().id()
                + " §8| §f" + state.mapProgress() + "§7/§f" + state.config().mapProgressTarget()
                + " §8| §7" + state.scoreSummary());
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
