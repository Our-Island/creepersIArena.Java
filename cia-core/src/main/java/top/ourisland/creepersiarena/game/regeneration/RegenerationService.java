package top.ourisland.creepersiarena.game.regeneration;

import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbility;
import top.ourisland.creepersiarena.api.ability.IAbilityConfigView;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.game.GameManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static top.ourisland.creepersiarena.utils.PlayerMotionChecks.isServerSideGrounded;

public final class RegenerationService implements IAbility {

    private final Logger logger;
    private final PlayerSessionStore sessions;
    private final GameManager gameManager;
    private final IAbilityGate abilities;
    private final Map<UUID, RegenerationState> states = new HashMap<>();

    private RegenerationConfig config = RegenerationConfig.defaults();

    public RegenerationService(
            Logger logger,
            PlayerSessionStore sessions,
            GameManager gameManager,
            IAbilityGate abilities
    ) {
        this.logger = logger;
        this.sessions = sessions;
        this.gameManager = gameManager;
        this.abilities = abilities;
        reload(abilities.config(CoreAbilities.RESTING_REGENERATION));
    }

    @Override
    public top.ourisland.creepersiarena.api.ability.AbilityId id() {
        return CoreAbilities.RESTING_REGENERATION;
    }

    @Override
    public void reload(IAbilityConfigView view) {
        config = RegenerationConfig.load(view, logger);
    }

    public void reloadConfig() {
        reload(abilities.config(CoreAbilities.RESTING_REGENERATION));
    }

    public void clearAll() {
        if (config.clearEffectOnBreak()) {
            for (var playerId : states.keySet()) {
                var player = Bukkit.getPlayer(playerId);
                if (player != null) player.removePotionEffect(PotionEffectType.REGENERATION);
            }
        }
        states.clear();
    }

    public RegenerationConfig config() {
        return config;
    }

    public void tick() {
        var currentConfig = config;
        for (var player : Bukkit.getOnlinePlayers()) {
            tickPlayer(player, currentConfig);
        }
    }

    private void tickPlayer(Player player, RegenerationConfig currentConfig) {
        if (!abilities.isEnabled(CoreAbilities.RESTING_REGENERATION, player, "regeneration_tick")) {
            breakRest(player, RegenerationBreakReason.NO_LONGER_ELIGIBLE);
            return;
        }

        if (!canRegenerate(player, currentConfig)) {
            breakRest(player, RegenerationBreakReason.NO_LONGER_ELIGIBLE);
            return;
        }

        var state = states.computeIfAbsent(
                player.getUniqueId(),
                _ -> new RegenerationState()
        );
        int currentTick = state.advance();

        for (var stage : currentConfig.stages()) {
            if (stage.tick() != currentTick || !state.markStageFired(stage.tick())) continue;
            applyStage(player, stage);
        }
    }

    public void breakRest(
            @Nullable Player player,
            @Nullable RegenerationBreakReason reason
    ) {
        if (player == null) return;
        var removed = states.remove(player.getUniqueId());
        if (removed == null) return;
        if (removed.restingTicks() <= 0) return;

        if (config.clearEffectOnBreak()) {
            player.removePotionEffect(PotionEffectType.REGENERATION);
        }
    }

    private boolean canRegenerate(Player player, RegenerationConfig currentConfig) {
        PlayerSession session = sessions.get(player);
        if (session == null) return false;
        if (currentConfig.requireInGame() && session.state() != PlayerState.IN_GAME) return false;

        GameSession active = gameManager.active();
        if (active == null || !active.players().contains(player.getUniqueId())) return false;

        if (currentConfig.requireTeam()) {
            Integer selectedTeam = session.selectedTeam();
            if (selectedTeam == null || !currentConfig.validTeams().contains(selectedTeam)) return false;
        }

        return player.isSneaking() && isRestingStill(player, currentConfig);
    }

    private void applyStage(Player player, RegenerationStage stage) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                stage.durationTicks(),
                stage.amplifier(),
                false,
                true,
                true
        ));

        try {
            player.playSound(
                    player.getLocation(),
                    stage.sound(),
                    SoundCategory.PLAYERS,
                    stage.volume(),
                    stage.pitch()
            );
        } catch (IllegalArgumentException exception) {
            logger.warn("[Regeneration] Invalid configured sound '{}': {}", stage.sound(), exception.getMessage());
        }
    }

    private boolean isRestingStill(Player player, RegenerationConfig currentConfig) {
        if (currentConfig.requireOnGround() && !isServerSideGrounded(player)) return false;
        if (player.isSprinting()) return false;
        if (player.isSwimming()) return false;
        if (player.isGliding()) return false;
        if (player.isFlying()) return false;

        Vector velocity = player.getVelocity();
        double horizontal = Math.hypot(velocity.getX(), velocity.getZ());
        if (horizontal > currentConfig.stationaryHorizontalEpsilon()) return false;

        return Math.abs(velocity.getY()) <= currentConfig.maxVerticalDelta();
    }

    public void clear(Player player) {
        if (player == null) return;
        states.remove(player.getUniqueId());
    }

}
