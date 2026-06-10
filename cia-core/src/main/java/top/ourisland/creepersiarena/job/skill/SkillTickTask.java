package top.ourisland.creepersiarena.job.skill;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.mode.context.ModePlayerContext;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.api.skill.event.SkillContext;
import top.ourisland.creepersiarena.api.skill.event.impl.TickEvent;
import top.ourisland.creepersiarena.core.bootstrap.module.SkillModule;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.mutation.ScaledTickAccumulator;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRuntime;
import top.ourisland.creepersiarena.job.skill.ui.SkillHotbarRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class SkillTickTask {

    private final PlayerSessionStore sessions;
    private final Supplier<GameManager> gameManager;
    private final SkillRegistry registry;
    private final SkillRuntime runtime;
    private final Plugin plugin;
    private final SkillHotbarRenderer renderer;
    private final DoubleSupplier tickScale;
    private final IntSupplier maxStepsPerRun;
    private final Supplier<IAbilityGate> abilities;
    private final ScaledTickAccumulator scaledClock = new ScaledTickAccumulator();

    private final AtomicLong tick = new AtomicLong(0);

    public SkillTickTask(
            PlayerSessionStore sessions,
            Supplier<GameManager> gameManager,
            SkillRegistry registry,
            SkillRuntime runtime,
            Plugin plugin,
            SkillHotbarRenderer renderer,
            DoubleSupplier tickScale,
            IntSupplier maxStepsPerRun,
            Supplier<IAbilityGate> abilities
    ) {
        this.sessions = sessions;
        this.gameManager = gameManager;
        this.registry = registry;
        this.runtime = runtime;
        this.plugin = plugin;
        this.renderer = renderer;
        this.tickScale = tickScale == null ? () -> 1.0D : tickScale;
        this.maxStepsPerRun = maxStepsPerRun == null ? () -> 1 : maxStepsPerRun;
        this.abilities = abilities == null ? () -> null : abilities;
    }

    public long nowTick() {
        return tick.get();
    }

    /**
     * Executes one skill tick.
     *
     * <p>Scheduled via Paper/Folia schedulers from {@link SkillModule}.
     */
    public void tick() {
        int steps = scaledClock.steps(tickScale.getAsDouble(), Math.max(1, maxStepsPerRun.getAsInt()));
        if (steps <= 0) return;

        var eligiblePlayers = eligiblePlayers();
        long now = tick.get();
        for (int i = 0; i < steps; i++) {
            now = tick.incrementAndGet();
            runSkillTick(now, eligiblePlayers);
        }

        renderHotbars(now, eligiblePlayers);
    }

    private List<Player> eligiblePlayers() {
        var out = new ArrayList<Player>();
        for (var p : Bukkit.getOnlinePlayers()) {
            var s = sessions.get(p);
            if (s == null || s.state() != PlayerState.IN_GAME) continue;
            if (!abilityEnabled(CoreAbilities.SKILL_RUNTIME, p, "skill_runtime")) continue;
            if (!allowsGameplaySkills(p, s)) continue;
            out.add(p);
        }
        return out;
    }

    private void runSkillTick(
            long now,
            List<Player> eligiblePlayers
    ) {
        for (var p : eligiblePlayers) {
            runtime.handle(new SkillContext(
                    p,
                    plugin,
                    new TickEvent(now),
                    null,
                    null,
                    now,
                    runtime.skillConfig()
            ));
        }
    }

    private void renderHotbars(
            long now,
            List<Player> eligiblePlayers
    ) {
        for (var p : eligiblePlayers) {
            if (!abilityEnabled(CoreAbilities.SKILL_HOTBAR, p, "skill_hotbar")) continue;
            renderer.render(p, registry.skillsOf(p), now);
        }
    }

    private boolean abilityEnabled(
            AbilityId id,
            Player player,
            String reason
    ) {
        var gate = abilities.get();
        if (gate == null) return false;
        return gate.isEnabled(id, player, reason);
    }

    private boolean allowsGameplaySkills(Player player, PlayerSession session) {
        GameManager manager = gameManager == null ? null : gameManager.get();
        if (manager == null || manager.active() == null || manager.playerFlow() == null || manager.runtime() == null) {
            return true;
        }

        if (!manager.active().players().contains(player.getUniqueId())) {
            return false;
        }

        var ctx = new ModePlayerContext(
                manager.runtime(),
                manager.active(),
                player,
                session,
                player.getLocation()
        );
        try {
            return manager.playerFlow().allowGameplaySkillRuntime(ctx);
        } catch (Throwable _) {
            return false;
        }
    }

}
