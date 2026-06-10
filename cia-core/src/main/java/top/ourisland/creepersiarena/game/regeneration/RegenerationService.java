package top.ourisland.creepersiarena.game.regeneration;

import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.*;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.api.game.rest.IRestStateService;
import top.ourisland.creepersiarena.game.GameManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static top.ourisland.creepersiarena.utils.PlayerMotionChecks.isServerSideGrounded;

public final class RegenerationService implements IAbility, IRestStateService {

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
    public AbilityId id() {
        return CoreAbilities.RESTING_REGENERATION;
    }

    @Override
    public void reload(IAbilityConfigView view) {
        config = RegenerationConfig.load(view, logger);
    }

    public void reloadConfig() {
        reload(abilities.config(CoreAbilities.RESTING_REGENERATION));
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

        currentConfig.stages().stream()
                .filter(stage -> stage.tick() == currentTick && state.markStageFired(stage.tick()))
                .forEach(stage -> applyStage(player, stage));
    }

    public void breakRest(
            @Nullable Player player,
            @Nullable RegenerationBreakReason reason
    ) {
        breakRest(player, reason == null ? null : reason.name());
    }

    private boolean canRegenerate(Player player, RegenerationConfig currentConfig) {
        var session = sessions.get(player);
        if (session == null) return false;
        if (currentConfig.requireInGame() && session.state() != PlayerState.IN_GAME) return false;

        var active = gameManager.active();
        if (active == null || !active.players().contains(player.getUniqueId())) return false;

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

    @Override
    public void breakRest(
            @Nullable Player player,
            @Nullable String reason
    ) {
        if (player == null) return;
        var removed = states.remove(player.getUniqueId());
        if (removed == null) return;
        if (removed.restingTicks() <= 0) return;

        if (config.clearEffectOnBreak()) {
            player.removePotionEffect(PotionEffectType.REGENERATION);
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

    @Override
    public void clearRest(@Nullable Player player) {
        if (player == null) return;
        states.remove(player.getUniqueId());
    }

    @Override
    public void clearAllRest() {
        clearAll();
    }

    public void clearAll() {
        if (config.clearEffectOnBreak()) {
            states.keySet().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(player -> player.removePotionEffect(PotionEffectType.REGENERATION));
        }
        states.clear();
    }

    public void clear(Player player) {
        clearRest(player);
    }

}
