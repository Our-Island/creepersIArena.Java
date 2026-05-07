package top.ourisland.creepersiarena.game.mode.impl.battle;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.event.ArenaPlayerDeathResolvedEvent;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.job.utils.BuiltinAttributeUtils;
import top.ourisland.creepersiarena.utils.Msg;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Battle-owned respawn lobby presentation.
 * <p>
 * Core still owns the generic respawn decision and countdown. This listener overlays the old datapack-style battle
 * feedback while the player is in the generic RESPAWN stage: temporary max-health recovery, resistance, healing,
 * actionbar text and sounds. No battle coordinates or countdown rules are written into core.
 */
public final class BattleRespawnPresentation implements Listener {

    private static final double START_HEALTH = 4.0D;
    private static final double FULL_HEALTH = 20.0D;
    private static final double HEALTH_STEP = 2.0D;

    private final Plugin plugin;
    private final GameManager gameManager;
    private final PlayerSessionStore sessions;
    private final Map<UUID, ScheduledTask> tasks = new HashMap<>();
    private final Map<UUID, Integer> pendingRespawns = new HashMap<>();

    public BattleRespawnPresentation(
            Plugin plugin,
            GameManager gameManager,
            PlayerSessionStore sessions
    ) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.sessions = sessions;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeathResolved(ArenaPlayerDeathResolvedEvent event) {
        BattleState state = activeBattle();
        if (state == null) return;

        var victim = event.result().victim();
        if (!state.isFighter(victim)) return;

        pendingRespawns.put(victim.getUniqueId(), Math.max(0, state.config().respawnTimeSeconds()));
        cancelTask(victim.getUniqueId());
    }

    private BattleState activeBattle() {
        GameSession session = gameManager.active();
        if (session == null || !session.mode().equals(BattleState.TYPE)) return null;
        if (!(gameManager.timeline() instanceof BattleTimeline timeline)) return null;
        return timeline.state();
    }

    private void cancelTask(UUID playerId) {
        if (playerId == null) return;
        ScheduledTask old = tasks.remove(playerId);
        if (old != null) old.cancel();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        Integer configuredSeconds = pendingRespawns.remove(player.getUniqueId());
        if (configuredSeconds == null) return;

        player.getScheduler().runDelayed(
                plugin,
                _ -> startPresentation(player, configuredSeconds),
                null,
                1L
        );
    }

    private void startPresentation(Player player, int configuredSeconds) {
        if (!isBattleRespawn(player)) return;

        var playerId = player.getUniqueId();
        cancelTask(playerId);

        applyStep(player, configuredSeconds);
        var task = player.getScheduler().runAtFixedRate(
                plugin,
                scheduled -> {
                    if (!player.isOnline() || !isBattleRespawn(player)) {
                        scheduled.cancel();
                        tasks.remove(playerId);
                        restore(player);
                        return;
                    }

                    var session = sessions.get(player);
                    int remaining = session == null ? 0 : session.respawnSecondsRemaining();
                    if (remaining <= 0) {
                        scheduled.cancel();
                        tasks.remove(playerId);
                        restore(player);
                        return;
                    }

                    applyStep(player, remaining);
                },
                null,
                20L,
                20L
        );
        tasks.put(playerId, task);
    }

    private boolean isBattleRespawn(Player player) {
        if (player == null) return false;
        BattleState state = activeBattle();
        if (state == null) return false;

        var session = sessions.get(player);
        return session != null
                && session.state() == PlayerState.RESPAWN
                && state.session().players().contains(player.getUniqueId());
    }

    private void applyStep(Player player, int remainingSeconds) {
        int totalSeconds = Math.max(1, configuredTotalSeconds(remainingSeconds));
        int elapsed = Math.max(0, totalSeconds - Math.max(0, remainingSeconds));
        double maxHealth = Math.min(FULL_HEALTH, START_HEALTH + elapsed * HEALTH_STEP);

        BuiltinAttributeUtils.setBaseValue(player, maxHealth, "max_health", "generic_max_health");
        double currentMax = currentMaxHealth(player, maxHealth);
        player.setHealth(Math.min(currentMax, Math.clamp(player.getHealth(), 1.0D, maxHealth)));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                40,
                4,
                true,
                false,
                false
        ));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INSTANT_HEALTH,
                1,
                1,
                true,
                false,
                false
        ));

        Msg.actionBar(player, Component.text("Battle 复活准备 ", NamedTextColor.RED)
                .append(Component.text(Math.max(0, remainingSeconds), NamedTextColor.YELLOW))
                .append(Component.text("s  ", NamedTextColor.GRAY))
                .append(Component.text((int) maxHealth + "/20 ❤", NamedTextColor.GREEN)));
        player.playSound(player.getLocation(), "minecraft:block.note_block.hat", SoundCategory.PLAYERS, 0.6F, 1.6F);
    }

    private void restore(Player player) {
        if (player == null || !player.isOnline()) return;
        BuiltinAttributeUtils.setBaseValue(player, FULL_HEALTH, "max_health", "generic_max_health");
        double currentMax = currentMaxHealth(player, FULL_HEALTH);
        if (player.getHealth() < currentMax) {
            player.setHealth(currentMax);
        }
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.playSound(player.getLocation(), "minecraft:entity.player.levelup", SoundCategory.PLAYERS, 0.8F, 1.2F);
        Msg.actionBar(player, Component.text("Battle 复活完成", NamedTextColor.GREEN));
    }

    private int configuredTotalSeconds(int currentRemaining) {
        BattleState state = activeBattle();
        if (state == null) return Math.max(1, currentRemaining);
        return Math.max(1, state.config().respawnTimeSeconds());
    }

    private double currentMaxHealth(Player player, double fallback) {
        Double value = BuiltinAttributeUtils.baseValue(player, "max_health", "generic_max_health");
        return value == null ? fallback : Math.max(1.0D, value);
    }

}
