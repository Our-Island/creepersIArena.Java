package top.ourisland.creepersiarena.game.mutation;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbility;
import top.ourisland.creepersiarena.api.ability.IAbilityConfigView;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mutation.*;
import top.ourisland.creepersiarena.game.GameManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MutationService implements IAbility, IMutationEffectContext {

    private final Logger logger;
    private final GameManager gameManager;
    private final IAbilityGate abilities;
    private final MutationRegistry registry;
    private final ScaledTickAccumulator activeClock = new ScaledTickAccumulator();
    private final MutationState state = new MutationState();

    private MutationConfig config = MutationConfig.defaults();

    public MutationService(
            Logger logger,
            GameManager gameManager,
            IAbilityGate abilities,
            MutationRegistry registry
    ) {
        this.logger = logger;
        this.gameManager = gameManager;
        this.abilities = abilities;
        this.registry = registry;
        reload(abilities.config(CoreAbilities.MUTATION));
    }

    private boolean abilityEnabled() {
        return abilities.isEnabledForGame(CoreAbilities.MUTATION, "mutation");
    }

    public void reset(MutationResetReason reason) {
        boolean wasActive = state.active();
        var activeType = state.activeType();
        var activeEffect = registry.get(activeType);

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

    @Override
    public top.ourisland.creepersiarena.api.ability.AbilityId id() {
        return CoreAbilities.MUTATION;
    }

    @Override
    public void reload(IAbilityConfigView view) {
        config = MutationConfig.load(view, logger);
        var section = view == null ? null : view.settingsSection();
        registry.reloadAll(section);

        if (!abilityEnabled()) {
            reset(MutationResetReason.DISABLED);
            return;
        }

        if (state.active()) {
            var activeEffect = registry.get(state.activeType());
            if (activeEffect == null || !activeEffect.enabled()) reset(MutationResetReason.RELOAD);
        }
    }

    public void reloadConfig() {
        reload(abilities.config(CoreAbilities.MUTATION));
    }

    public void tick() {
        var currentConfig = config;
        if (!abilityEnabled()) {
            reset(MutationResetReason.DISABLED);
            return;
        }

        if (!hasEligibleActiveGame()) {
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

    private boolean hasEligibleActiveGame() {
        GameSession active = gameManager.active();
        if (active == null) return false;
        return abilities.isEnabledForGame(CoreAbilities.MUTATION, "mutation_eligible_game");
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
        var effect = registry.get(state.activeType());
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

        var effect = pickEnabledEffect(currentConfig);
        if (effect == null) {
            rollFailure(currentConfig);
            return;
        }

        var result = startEffect(effect);
        if (result.kind() != MutationTriggerResult.Kind.STARTED) rollFailure(currentConfig);
    }

    private double currentLogicalScale() {
        if (!state.active()) return 1.0D;
        var effect = registry.get(state.activeType());
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

    private IMutationEffect pickEnabledEffect(MutationConfig currentConfig) {
        var context = candidateContext();
        var candidates = new ArrayList<WeightedEffect>();
        int totalWeight = 0;

        for (var effect : registry.effects()) {
            if (effect == null || !effect.enabled()) continue;
            try {
                if (!effect.canStart(context)) continue;
                int weight = Math.max(0, effect.weight(context));
                if (weight == 0) continue;
                candidates.add(new WeightedEffect(effect, weight));
                totalWeight += weight;
            } catch (Throwable t) {
                logger.warn("[Mutation] Candidate evaluation failed for {}: {}", effect.type(), t.getMessage(), t);
            }
        }

        if (candidates.isEmpty() || totalWeight <= 0) return null;

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        for (var candidate : candidates) {
            roll -= candidate.weight();
            if (roll < 0) return candidate.effect();
        }

        return candidates.getLast().effect();
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
        if (player == null) return false;
        return abilities.isEnabled(CoreAbilities.MUTATION, player, "mutation_target");
    }

    private MutationCandidateContext candidateContext() {
        var active = gameManager.active();
        String modeId = active == null || active.mode() == null ? "" : active.mode().id();
        String arenaId = active == null || active.arena() == null ? "" : active.arena().id();

        return new MutationCandidateContext(
                active,
                modeId,
                arenaId,
                state.idleCounterTicks(),
                targets(MutationTargetScope.ACTIVE_GAME_PLAYERS)
        );
    }

    public MutationTriggerResult trigger() {
        if (state.active()) {
            var activeType = state.activeType();
            reset(MutationResetReason.MANUAL);
            return MutationTriggerResult.cancelled(activeType);
        }

        var currentConfig = config;
        if (!abilityEnabled()) return MutationTriggerResult.disabled();
        if (!hasEligibleActiveGame()) return MutationTriggerResult.noEligibleGame();

        var marker = currentMarker();
        if (marker == null) return MutationTriggerResult.noEligibleGame();
        state.sessionMarker(marker);

        var effect = pickEnabledEffect(currentConfig);
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

    public MutationConfig config() {
        return config;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public World world() {
        var active = gameManager.active();
        return active == null || active.arena() == null ? null : active.arena().world();
    }

    @Override
    public GameSession game() {
        return gameManager.active();
    }

    @Override
    public MutationClockMode clockMode() {
        return config.clockMode();
    }

    @Override
    public Collection<Player> targets(MutationTargetScope scope) {
        if (scope == MutationTargetScope.ALL_ONLINE) {
            return List.copyOf(Bukkit.getOnlinePlayers());
        }

        var active = gameManager.active();
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

    public String statusLine(boolean adminEnabled) {
        var activeEffect = registry.get(state.activeType());
        String detail = activeEffect == null ? "" : activeEffect.status(this);
        if (!detail.isBlank()) detail = " " + detail;
        var view = abilities.config(CoreAbilities.MUTATION);
        return "Mutation: enabled=%s configEnabled=%s adminEnabled=%s active=%s idle=%d remaining=%d mode=%s effects=%d%s".formatted(
                abilityEnabled(),
                view.enabled(false),
                adminEnabled,
                state.activeType(),
                state.idleCounterTicks(),
                state.remainingTicks(),
                config.clockMode(),
                registry.size(),
                detail
        );
    }

    public boolean effectiveEnabled() {
        return abilityEnabled();
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
        registry.clearPlayer(player);
    }

    private record WeightedEffect(
            IMutationEffect effect,
            int weight
    ) {

    }

}
