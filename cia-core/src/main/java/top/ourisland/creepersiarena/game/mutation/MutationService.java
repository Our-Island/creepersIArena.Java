package top.ourisland.creepersiarena.game.mutation;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.game.GameManager;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class MutationService implements IMutationEffectContext {

    private final Logger logger;
    private final ConfigManager configManager;
    private final GameManager gameManager;
    private final ScaledTickAccumulator activeClock = new ScaledTickAccumulator();
    private final MutationState state = new MutationState();
    private final Map<MutationType, IMutationEffect> effects = new LinkedHashMap<>();

    private MutationConfig config = MutationConfig.defaults();
    private boolean adminEnabled = true;

    public MutationService(
            Logger logger,
            ConfigManager configManager,
            GameManager gameManager
    ) {
        this.logger = logger;
        this.configManager = configManager;
        this.gameManager = gameManager;
        reloadConfig();
    }

    public void reloadConfig() {
        config = MutationConfig.load(configManager, logger);
        var section = mutationSection();
        for (var effect : effects.values()) {
            reloadEffect(effect, section);
        }

        if (!effectiveEnabled(config)) {
            reset(MutationResetReason.DISABLED);
            return;
        }

        if (state.active()) {
            var activeEffect = effects.get(state.activeType());
            if (activeEffect == null || !activeEffect.enabled()) reset(MutationResetReason.RELOAD);
        }
    }

    private @Nullable ConfigurationSection mutationSection() {
        try {
            return MutationConfig.loadSection(configManager);
        } catch (Throwable t) {
            logger.warn("[Mutation] Failed to load effect config section: {}", t.getMessage(), t);
            return null;
        }
    }

    private void reloadEffect(
            IMutationEffect effect,
            @Nullable ConfigurationSection section
    ) {
        try {
            effect.reload(section, logger);
        } catch (Throwable t) {
            logger.warn("[Mutation] Failed to reload {}: {}", effect.type(), t.getMessage(), t);
        }
    }

    private boolean effectiveEnabled(MutationConfig currentConfig) {
        return adminEnabled && currentConfig.enabled();
    }

    public void reset(MutationResetReason reason) {
        boolean wasActive = state.active();
        var activeType = state.activeType();
        var activeEffect = effects.get(activeType);

        if (activeEffect != null) {
            try {
                activeEffect.reset(this, reason, wasActive);
            } catch (Throwable t) {
                logger.warn("[Mutation] Effect reset failed for {}: {}", activeType, t.getMessage(), t);
            }
        }

        state.clear();
        activeClock.reset();
    }

    public void registerMutation(IMutationEffect effect) {
        if (effect == null || effect.type() == null || effect.type().isNone()) return;
        effects.put(effect.type(), effect);
        reloadEffect(effect);
    }

    private void reloadEffect(IMutationEffect effect) {
        reloadEffect(effect, mutationSection());
    }

    public void tick() {
        var currentConfig = config;
        if (!effectiveEnabled(currentConfig)) {
            reset(MutationResetReason.DISABLED);
            return;
        }

        if (!hasEligibleActiveGame(currentConfig)) {
            reset(MutationResetReason.NO_ELIGIBLE_GAME);
            state.sessionMarker(null);
            return;
        }

        var marker = currentMarker();
        if (marker == null) {
            reset(MutationResetReason.NO_ELIGIBLE_GAME);
            state.sessionMarker(null);
            return;
        }
        if (state.sessionMarker() == null) {
            state.sessionMarker(marker);
        } else if (!state.sessionMarker().equals(marker)) {
            reset(MutationResetReason.GAME_CHANGED);
            state.sessionMarker(marker);
        }

        if (!state.active()) {
            tickIdle(currentConfig);
            return;
        }

        tickActive(currentConfig);
    }

    private boolean hasEligibleActiveGame(MutationConfig currentConfig) {
        GameSession active = gameManager.active();
        if (active == null) return false;
        if (!currentConfig.isEligibleMode(active.mode().id())) return false;
        if (gameManager.timeline() instanceof IMutationEligibility eligibility) {
            try {
                return eligibility.allowMutation();
            } catch (Throwable t) {
                logger.warn("[Mutation] Timeline mutation eligibility failed: {}", t.getMessage(), t);
                return false;
            }
        }
        return true;
    }

    private MutationState.GameSessionMarker currentMarker() {
        GameSession active = gameManager.active();
        if (active == null || active.arena() == null || active.mode() == null) return null;
        return new MutationState.GameSessionMarker(active.mode().id(), active.arena().id());
    }

    private void tickIdle(MutationConfig currentConfig) {
        state.addIdleTicks(1);
        if (state.idleCounterTicks() < currentConfig.idleAttemptTicks()) return;
        tryStartMutation(currentConfig);
    }

    private void tickActive(MutationConfig currentConfig) {
        var effect = effects.get(state.activeType());
        if (effect == null) {
            reset(MutationResetReason.MANUAL);
            return;
        }

        int steps = activeClock.steps(currentLogicalScale(), currentConfig.maxLogicalStepsPerRun());
        if (steps <= 0) return;

        try {
            effect.tick(this, steps);
        } catch (Throwable t) {
            logger.warn("[Mutation] Tick failed for {}: {}", state.activeType(), t.getMessage(), t);
            reset(MutationResetReason.MANUAL);
            return;
        }

        state.subtractRemainingTicks(steps);
        if (state.remainingTicks() <= 0) reset(MutationResetReason.EXPIRED);
    }

    private void tryStartMutation(MutationConfig currentConfig) {
        boolean hasTarget = !targets(MutationTargetScope.ACTIVE_GAME_PLAYERS).isEmpty();
        if (currentConfig.requireOnlineTarget() && !hasTarget) {
            rollFailure(currentConfig);
            return;
        }

        var selection = currentConfig.selection();
        int roll = hasTarget
                ? randomInt(selection.onlineRollMin(), selection.onlineRollMax())
                : randomInt(selection.offlineRollMin(), selection.offlineRollMax());

        if (roll < selection.startMinInclusive()) {
            rollFailure(currentConfig);
            return;
        }

        var effect = pickEnabledEffect();
        if (effect == null) {
            rollFailure(currentConfig);
            return;
        }

        var result = startEffect(effect);
        if (result.kind() != MutationTriggerResult.Kind.STARTED) rollFailure(currentConfig);
    }

    private double currentLogicalScale() {
        if (!state.active()) return 1.0D;
        var effect = effects.get(state.activeType());
        if (effect == null) return 1.0D;
        double scale = effect.logicalScale(this);
        if (Double.isNaN(scale) || Double.isInfinite(scale) || scale <= 0.0D) return 1.0D;
        return Math.max(1.0D, scale);
    }

    private void rollFailure(MutationConfig currentConfig) {
        state.idleCounterTicks(currentConfig.failedRollCounterValue());
    }

    private int randomInt(
            int min,
            int max
    ) {
        if (max <= min) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private IMutationEffect pickEnabledEffect() {
        var candidates = new ArrayList<IMutationEffect>();
        for (IMutationEffect effect : effects.values()) {
            if (effect.enabled()) candidates.add(effect);
        }
        if (candidates.isEmpty()) return null;
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    private MutationTriggerResult startEffect(IMutationEffect effect) {
        if (effect == null) return MutationTriggerResult.noEffect();

        try {
            var result = effect.start(this);
            state.activeType(effect.type());
            state.idleCounterTicks(0);
            state.remainingTicks(result.durationTicks());
            logger.info("[Mutation] Started {} for arena {}.", effect.type(), currentMarker());
            return MutationTriggerResult.started(effect.type());
        } catch (Throwable t) {
            logger.warn("[Mutation] Failed to start {}: {}", effect.type(), t.getMessage(), t);
            try {
                effect.reset(this, MutationResetReason.MANUAL, false);
            } catch (Throwable resetFailure) {
                logger.warn("[Mutation] Failed to clean up {} after start failure: {}",
                        effect.type(),
                        resetFailure.getMessage(),
                        resetFailure
                );
            }
            state.clear();
            activeClock.reset();
            return MutationTriggerResult.failed(effect.type(), "Failed to start mutation " + effect.type() + ": " + t.getMessage());
        }
    }

    private boolean allowMutationTarget(Player player) {
        if (gameManager.timeline() instanceof IMutationEligibility eligibility) {
            try {
                return eligibility.allowMutationTarget(player);
            } catch (Throwable t) {
                logger.warn("[Mutation] Timeline mutation target eligibility failed: {}", t.getMessage(), t);
                return false;
            }
        }
        return true;
    }

    public MutationTriggerResult trigger() {
        if (state.active()) {
            var activeType = state.activeType();
            reset(MutationResetReason.MANUAL);
            return MutationTriggerResult.cancelled(activeType);
        }

        var currentConfig = config;
        if (!effectiveEnabled(currentConfig)) return MutationTriggerResult.disabled();
        if (!hasEligibleActiveGame(currentConfig)) return MutationTriggerResult.noEligibleGame();

        var marker = currentMarker();
        if (marker == null) return MutationTriggerResult.noEligibleGame();
        state.sessionMarker(marker);

        var effect = pickEnabledEffect();
        if (effect == null) return MutationTriggerResult.noEffect();
        return startEffect(effect);
    }

    public void nudgeFromDeath() {
        var currentConfig = config;
        nudge(currentConfig.deathNudgeTicks());
    }

    public void nudge(int ticks) {
        if (ticks <= 0) return;
        if (state.active()) {
            state.subtractRemainingTicks(ticks);
            if (state.remainingTicks() <= 0) reset(MutationResetReason.EXPIRED);
            return;
        }
        state.addIdleTicks(ticks);
    }

    public void setAdminEnabled(boolean enabled) {
        adminEnabled = enabled;
        if (!enabled) reset(MutationResetReason.ADMIN_DISABLED);
    }

    public boolean adminEnabled() {
        return adminEnabled;
    }

    @Override
    public MutationConfig config() {
        return config;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public Collection<Player> targets(MutationTargetScope scope) {
        if (scope == MutationTargetScope.ALL_ONLINE) {
            return List.copyOf(Bukkit.getOnlinePlayers());
        }

        GameSession active = gameManager.active();
        if (active == null) return List.of();

        var out = new ArrayList<Player>();
        for (var playerId : active.players()) {
            var player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) continue;
            if (!allowMutationTarget(player)) continue;
            out.add(player);
        }
        return out;
    }

    public String statusLine() {
        var activeEffect = effects.get(state.activeType());
        String detail = activeEffect == null ? "" : activeEffect.status(this);
        if (!detail.isBlank()) detail = " " + detail;
        return "Mutation: enabled=%s configEnabled=%s adminEnabled=%s active=%s idle=%d remaining=%d mode=%s%s".formatted(
                effectiveEnabled(),
                config.enabled(),
                adminEnabled,
                state.activeType(),
                state.idleCounterTicks(),
                state.remainingTicks(),
                config.clockMode(),
                detail
        );
    }

    public boolean effectiveEnabled() {
        return effectiveEnabled(config);
    }

    public double gameSecondScale() {
        return currentLogicalScale();
    }

    public double skillTickScale() {
        return currentLogicalScale();
    }

    public double serverTickScale() {
        return currentLogicalScale();
    }

    public int maxLogicalStepsPerRun() {
        return config.maxLogicalStepsPerRun();
    }

    public void clearPlayer(@Nullable Player player) {
        for (IMutationEffect effect : effects.values()) {
            try {
                effect.clearPlayer(player);
            } catch (Throwable t) {
                logger.warn("[Mutation] Failed to clear player for {}: {}", effect.type(), t.getMessage(), t);
            }
        }
    }

}
