package top.ourisland.creepersiarena.game.mode.impl.battle;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
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
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilityChecks;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.utils.AttributeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Battle-owned respawn lobby presentation.
 * <p>
 * Core still owns the generic respawn decision and countdown. This listener overlays the old datapack-style battle
 * feedback while the player is in the generic RESPAWN stage: temporary max-health recovery, resistance, healing and
 * sounds. No battle coordinates or countdown rules are written into core.
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
    private final Map<UUID, Integer> presentationTicks = new HashMap<>();

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
        if (!abilityEnabled(state, victim, "battle_respawn_presentation")) return;
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

    private boolean abilityEnabled(BattleState state, Player player, String reason) {
        return state != null && DefaultContentAbilityChecks.enabled(
                state.runtime(),
                state.session(),
                player,
                DefaultContentAbilities.BATTLE_RESPAWN_PRESENTATION,
                null,
                reason
        );
    }

    private void cancelTask(UUID playerId) {
        if (playerId == null) return;
        ScheduledTask old = tasks.remove(playerId);
        if (old != null) old.cancel();
        presentationTicks.remove(playerId);
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

        int totalTicks = Math.max(1, configuredTotalSeconds(configuredSeconds) * 20);
        presentationTicks.put(playerId, 0);
        player.playSound(player.getLocation(), "minecraft:entity.wither.spawn", SoundCategory.PLAYERS, 1.0F, 1.5F);
        applyInitialHealth(player);

        var task = player.getScheduler().runAtFixedRate(
                plugin,
                scheduled -> {
                    if (!player.isOnline() || !isBattleRespawn(player)) {
                        scheduled.cancel();
                        tasks.remove(playerId);
                        presentationTicks.remove(playerId);
                        restore(player, !isInGame(player));
                        return;
                    }

                    var session = sessions.get(player);
                    int remaining = session == null ? 0 : session.respawnSecondsRemaining();
                    int elapsedTicks = presentationTicks.merge(playerId, 1, Integer::sum);

                    playAmbient(player);
                    if (elapsedTicks % 20 == 0) {
                        applyStep(player, elapsedTicks / 20);
                    }

                    if (remaining <= 0 || elapsedTicks >= totalTicks) {
                        scheduled.cancel();
                        tasks.remove(playerId);
                        presentationTicks.remove(playerId);
                        restore(player, false);
                    }
                },
                null,
                1L,
                1L
        );
        tasks.put(playerId, task);
    }

    private boolean isBattleRespawn(Player player) {
        if (player == null) return false;
        BattleState state = activeBattle();
        if (state == null) return false;
        if (!abilityEnabled(state, player, "battle_respawn_presentation")) return false;

        var session = sessions.get(player);
        return session != null
                && session.state() == PlayerState.RESPAWN
                && state.session().players().contains(player.getUniqueId());
    }

    private void applyInitialHealth(Player player) {
        applyMaxHealth(player, START_HEALTH);
    }

    private void applyStep(Player player, int elapsedSeconds) {
        double maxHealth = Math.min(FULL_HEALTH, START_HEALTH + Math.max(0, elapsedSeconds) * HEALTH_STEP);
        applyMaxHealth(player, maxHealth);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                40,
                5,
                true,
                false,
                false
        ));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INSTANT_HEALTH,
                1,
                0,
                true,
                false,
                false
        ));
        player.playSound(player.getLocation(), "minecraft:item.lead.tied", SoundCategory.PLAYERS, 1.0F, 0.0F);
    }

    private void applyMaxHealth(Player player, double maxHealth) {
        AttributeUtils.setBaseValue(player, maxHealth, "max_health", "generic_max_health");
        double currentMax = currentMaxHealth(player, maxHealth);
        player.setHealth(Math.min(currentMax, Math.clamp(player.getHealth(), 1.0D, maxHealth)));
    }

    private void playAmbient(Player player) {
        player.playSound(player.getLocation(), "minecraft:block.beacon.ambient", SoundCategory.PLAYERS, 10.0F, 0.1F);
    }

    private void restore(Player player, boolean removeResistance) {
        if (player == null || !player.isOnline()) return;
        AttributeUtils.setBaseValue(player, FULL_HEALTH, "max_health", "generic_max_health");
        double currentMax = currentMaxHealth(player, FULL_HEALTH);
        if (player.getHealth() < currentMax) {
            player.setHealth(currentMax);
        }
        if (removeResistance) {
            player.removePotionEffect(PotionEffectType.RESISTANCE);
        }
    }

    private boolean isInGame(Player player) {
        var session = sessions.get(player);
        return session != null && session.state() == PlayerState.IN_GAME;
    }

    private int configuredTotalSeconds(int currentRemaining) {
        BattleState state = activeBattle();
        if (state == null) return Math.max(1, currentRemaining);
        return Math.max(1, state.config().respawnTimeSeconds());
    }

    private double currentMaxHealth(Player player, double fallback) {
        Double value = AttributeUtils.baseValue(player, "max_health", "generic_max_health");
        return value == null ? fallback : Math.max(1.0D, value);
    }

}
