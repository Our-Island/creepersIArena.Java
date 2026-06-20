package top.ourisland.creepersiarena.defaultcontent.game.mode.battle;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.config.StrictConfig;
import top.ourisland.creepersiarena.api.game.death.ArenaPlayerDeathResolvedEvent;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.utils.AttributeUtils;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilityChecks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Battle-owned respawn effects during the generic RESPAWN stage.
 * <p>
 * Respawn presentation is split at the ability level: recovery changes gameplay state (temporary max health, resistance
 * and healing), while visuals only play sounds. Either side can be disabled or tuned independently.
 */
public final class BattleRespawnEffectsListener implements Listener {

    private static final String RECOVERY_PATH = "game.abilities.cia.battle_respawn_recovery.settings";
    private static final String VISUALS_PATH = "game.abilities.cia.battle_respawn_visuals.settings";

    private final Plugin plugin;
    private final GameManager gameManager;
    private final PlayerSessionStore sessions;
    private final IAbilityGate abilities;
    private final Map<UUID, ScheduledTask> tasks = new HashMap<>();
    private final Map<UUID, Integer> pendingRespawns = new HashMap<>();
    private final Map<UUID, Integer> presentationTicks = new HashMap<>();

    public BattleRespawnEffectsListener(
            Plugin plugin,
            GameManager gameManager,
            PlayerSessionStore sessions,
            IAbilityGate abilities
    ) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.sessions = sessions;
        this.abilities = abilities;
    }

    private static ConfigurationSection sub(
            ConfigurationSection section,
            String key,
            String path
    ) {
        return StrictConfig.section(section, key, path);
    }

    private static double positive(
            ConfigurationSection section,
            String key,
            double fallback,
            String path
    ) {
        double value = StrictConfig.decimal(section, key, fallback, path);
        if (!(value > 0.0D)) throw new IllegalArgumentException("Invalid value at " + path + ": expected > 0");
        return value;
    }

    private static int nonNegative(
            ConfigurationSection section,
            String key,
            int fallback,
            String path
    ) {
        int value = StrictConfig.integer(section, key, fallback, path);
        if (value < 0) throw new IllegalArgumentException("Invalid value at " + path + ": expected >= 0");
        return value;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeathResolved(ArenaPlayerDeathResolvedEvent event) {
        var state = activeBattle();
        if (state == null) return;

        var victim = event.result().victim();
        if (!state.isFighter(victim)) return;
        if (!recoveryEnabled(state, victim, "battle_respawn_recovery")
                && !visualsEnabled(state, victim, "battle_respawn_visuals")) {
            return;
        }

        pendingRespawns.put(victim.getUniqueId(), Math.max(0, state.config().respawnTimeSeconds()));
        cancelTask(victim.getUniqueId(), false);
    }

    private @Nullable BattleState activeBattle() {
        var session = gameManager.active();
        if (session == null || !session.mode().equals(BattleState.TYPE)) return null;
        if (!(gameManager.timeline() instanceof BattleTimeline timeline)) return null;
        return timeline.state();
    }

    private boolean recoveryEnabled(
            BattleState state,
            Player player,
            String reason
    ) {
        return state != null && DefaultContentAbilityChecks.enabled(
                state.runtime(),
                state.session(),
                player,
                DefaultContentAbilities.BATTLE_RESPAWN_RECOVERY,
                null,
                reason
        );
    }

    private boolean visualsEnabled(
            BattleState state,
            Player player,
            String reason
    ) {
        return state != null && DefaultContentAbilityChecks.enabled(
                state.runtime(),
                state.session(),
                player,
                DefaultContentAbilities.BATTLE_RESPAWN_VISUALS,
                null,
                reason
        );
    }

    private void cancelTask(UUID playerId, boolean restore) {
        if (playerId == null) return;
        ScheduledTask old = tasks.remove(playerId);
        if (old != null) old.cancel();
        presentationTicks.remove(playerId);
        if (restore) {
            var player = org.bukkit.Bukkit.getPlayer(playerId);
            if (player != null) restore(player, true, recoveryConfig());
        }
    }

    private void restore(
            Player player,
            boolean removeResistance,
            RecoveryConfig config
    ) {
        if (player == null || !player.isOnline()) return;
        AttributeUtils.setBaseValue(player, config.fullHealth(), Attribute.MAX_HEALTH);
        double currentMax = currentMaxHealth(player, config.fullHealth());
        if (player.getHealth() < currentMax) player.setHealth(currentMax);
        if (removeResistance) player.removePotionEffect(PotionEffectType.RESISTANCE);
    }

    private @NonNull RecoveryConfig recoveryConfig() {
        var section = abilities == null
                ? null
                : abilities.config(DefaultContentAbilities.BATTLE_RESPAWN_RECOVERY).settingsSection();
        return RecoveryConfig.fromSection(section);
    }

    private double currentMaxHealth(Player player, double fallback) {
        var value = AttributeUtils.baseValue(player, Attribute.MAX_HEALTH);
        return value == null ? fallback : Math.max(1.0D, value);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        var configuredSeconds = pendingRespawns.remove(player.getUniqueId());
        if (configuredSeconds == null) return;

        player.getScheduler().runDelayed(
                plugin,
                _ -> startEffects(player, configuredSeconds),
                null,
                1L
        );
    }

    private void startEffects(Player player, int configuredSeconds) {
        if (!isBattleRespawn(player)) return;

        var state = activeBattle();
        var recovery = recoveryEnabled(state, player, "battle_respawn_recovery");
        var visuals = visualsEnabled(state, player, "battle_respawn_visuals");
        if (!recovery && !visuals) return;

        var recoveryConfig = recoveryConfig();
        var visualConfig = visualConfig();
        var playerId = player.getUniqueId();
        cancelTask(playerId, recovery);

        int totalTicks = Math.max(1, configuredTotalSeconds(configuredSeconds) * 20);
        presentationTicks.put(playerId, 0);
        if (visuals) play(player, visualConfig.startSound());
        if (recovery) applyInitialHealth(player, recoveryConfig);

        var task = player.getScheduler().runAtFixedRate(
                plugin,
                scheduled -> {
                    var currentState = activeBattle();
                    var stillRecovery = recoveryEnabled(currentState, player, "battle_respawn_recovery");
                    var stillVisuals = visualsEnabled(currentState, player, "battle_respawn_visuals");
                    if (!player.isOnline() || !isBattleRespawn(player) || (!stillRecovery && !stillVisuals)) {
                        scheduled.cancel();
                        tasks.remove(playerId);
                        presentationTicks.remove(playerId);
                        if (recovery) restore(player, !isInGame(player), recoveryConfig);
                        return;
                    }

                    var session = sessions.get(player);
                    int remaining = session == null ? 0 : session.respawnSecondsRemaining();
                    int elapsedTicks = presentationTicks.merge(playerId, 1, Integer::sum);

                    if (stillVisuals) play(player, visualConfig.ambientSound());
                    if (elapsedTicks % 20 == 0) {
                        if (stillRecovery) applyStep(player, elapsedTicks / 20, recoveryConfig);
                        if (stillVisuals) play(player, visualConfig.stepSound());
                    }

                    if (remaining <= 0 || elapsedTicks >= totalTicks) {
                        scheduled.cancel();
                        tasks.remove(playerId);
                        presentationTicks.remove(playerId);
                        if (recovery) restore(player, false, recoveryConfig);
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
        var state = activeBattle();

        var session = sessions.get(player);
        return state != null
                && session != null
                && session.state() == PlayerState.RESPAWN
                && state.session().players().contains(player.getUniqueId());
    }

    private void applyInitialHealth(Player player, RecoveryConfig config) {
        applyMaxHealth(player, config.startHealth());
    }

    private void applyStep(
            Player player,
            int elapsedSeconds,
            RecoveryConfig config
    ) {
        double maxHealth = Math.min(
                config.fullHealth(),
                config.startHealth() + Math.max(0, elapsedSeconds) * config.healthStepPerSecond()
        );
        applyMaxHealth(player, maxHealth);
        if (config.resistanceDurationTicks() > 0) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.RESISTANCE,
                    config.resistanceDurationTicks(),
                    config.resistanceAmplifier(),
                    true,
                    false,
                    false
            ));
        }
        if (config.healAmplifier() >= 0) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INSTANT_HEALTH,
                    1,
                    config.healAmplifier(),
                    true,
                    false,
                    false
            ));
        }
    }

    private void applyMaxHealth(Player player, double maxHealth) {
        AttributeUtils.setBaseValue(player, maxHealth, Attribute.MAX_HEALTH);
        double currentMax = currentMaxHealth(player, maxHealth);
        player.setHealth(Math.min(currentMax, Math.clamp(player.getHealth(), 1.0D, maxHealth)));
    }

    private void play(Player player, SoundConfig sound) {
        if (player == null || sound == null || sound.sound().isBlank()) return;
        try {
            player.playSound(player.getLocation(), sound.sound(), SoundCategory.PLAYERS, sound.volume(), sound.pitch());
        } catch (IllegalArgumentException _) {
        }
    }

    private boolean isInGame(Player player) {
        var session = sessions.get(player);
        return session != null && session.state() == PlayerState.IN_GAME;
    }

    private int configuredTotalSeconds(int currentRemaining) {
        var state = activeBattle();
        return Math.max(1, state.config().respawnTimeSeconds());
    }

    private VisualConfig visualConfig() {
        var section = abilities == null
                ? null
                : abilities.config(DefaultContentAbilities.BATTLE_RESPAWN_VISUALS).settingsSection();
        return VisualConfig.fromSection(section);
    }

    private record RecoveryConfig(
            double startHealth,
            double fullHealth,
            double healthStepPerSecond,
            int resistanceDurationTicks,
            int resistanceAmplifier,
            int healAmplifier
    ) {

        static RecoveryConfig fromSection(ConfigurationSection section) {
            double startHealth = positive(section, "start-health", 4.0D, RECOVERY_PATH + ".start-health");
            double fullHealth = positive(section, "full-health", 20.0D, RECOVERY_PATH + ".full-health");
            if (fullHealth < startHealth) {
                throw new IllegalArgumentException("Invalid value at " + RECOVERY_PATH
                        + ".full-health: expected >= start-health");
            }
            var resistance = sub(section, "resistance", RECOVERY_PATH + ".resistance");
            var instantHealth = sub(section, "instant-health", RECOVERY_PATH + ".instant-health");
            int healAmplifier = StrictConfig.integer(
                    instantHealth,
                    "amplifier",
                    0,
                    RECOVERY_PATH + ".instant-health.amplifier"
            );
            if (healAmplifier < -1) {
                throw new IllegalArgumentException("Invalid value at " + RECOVERY_PATH
                        + ".instant-health.amplifier: expected >= -1");
            }
            return new RecoveryConfig(
                    startHealth,
                    fullHealth,
                    positive(section, "health-step-per-second", 2.0D, RECOVERY_PATH + ".health-step-per-second"),
                    nonNegative(resistance, "duration-ticks", 40, RECOVERY_PATH + ".resistance.duration-ticks"),
                    nonNegative(resistance, "amplifier", 5, RECOVERY_PATH + ".resistance.amplifier"),
                    healAmplifier
            );
        }

    }

    private record VisualConfig(
            SoundConfig startSound,
            SoundConfig ambientSound,
            SoundConfig stepSound
    ) {

        static VisualConfig fromSection(ConfigurationSection section) {
            return new VisualConfig(
                    SoundConfig.fromSection(
                            sub(section, "start-sound", VISUALS_PATH + ".start-sound"),
                            VISUALS_PATH + ".start-sound",
                            "minecraft:entity.wither.spawn",
                            1.0F,
                            1.5F
                    ),
                    SoundConfig.fromSection(
                            sub(section, "ambient-sound", VISUALS_PATH + ".ambient-sound"),
                            VISUALS_PATH + ".ambient-sound",
                            "minecraft:block.beacon.ambient",
                            10.0F,
                            0.1F
                    ),
                    SoundConfig.fromSection(
                            sub(section, "step-sound", VISUALS_PATH + ".step-sound"),
                            VISUALS_PATH + ".step-sound",
                            "minecraft:item.lead.tied",
                            1.0F,
                            0.0F
                    )
            );
        }

    }

    private record SoundConfig(
            String sound,
            float volume,
            float pitch
    ) {

        static SoundConfig fromSection(
                ConfigurationSection section,
                String path,
                String fallbackSound,
                float fallbackVolume,
                float fallbackPitch
        ) {
            String sound = StrictConfig.string(section, "sound", fallbackSound, path + ".sound");
            if (sound.isBlank() || !sound.matches("[a-z0-9._-]+:[a-z0-9_./-]+")) {
                throw new IllegalArgumentException("Invalid value at " + path + ".sound: expected namespaced sound id");
            }
            double volume = StrictConfig.decimal(section, "volume", fallbackVolume, path + ".volume");
            if (volume < 0.0D)
                throw new IllegalArgumentException("Invalid value at " + path + ".volume: expected >= 0");
            double pitch = StrictConfig.decimal(section, "pitch", fallbackPitch, path + ".pitch");
            if (pitch < 0.0D || pitch > 2.0D) {
                throw new IllegalArgumentException("Invalid value at " + path + ".pitch: expected 0..2");
            }
            return new SoundConfig(sound, (float) volume, (float) pitch);
        }

    }

}
