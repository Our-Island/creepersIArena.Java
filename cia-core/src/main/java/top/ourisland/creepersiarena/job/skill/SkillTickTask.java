package top.ourisland.creepersiarena.job.skill;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.mode.context.ModePlayerContext;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.api.skill.event.SkillContext;
import top.ourisland.creepersiarena.api.skill.event.impl.TickEvent;
import top.ourisland.creepersiarena.core.bootstrap.module.SkillModule;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRuntime;
import top.ourisland.creepersiarena.job.skill.ui.SkillHotbarRenderer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public final class SkillTickTask {

    private final PlayerSessionStore sessions;
    private final Supplier<GameManager> gameManager;
    private final SkillRegistry registry;
    private final SkillRuntime runtime;
    private final org.bukkit.plugin.Plugin plugin;
    private final SkillHotbarRenderer renderer;

    private final AtomicLong tick = new AtomicLong(0);

    public SkillTickTask(
            PlayerSessionStore sessions,
            Supplier<GameManager> gameManager,
            SkillRegistry registry,
            SkillRuntime runtime,
            org.bukkit.plugin.Plugin plugin,
            SkillHotbarRenderer renderer
    ) {
        this.sessions = sessions;
        this.gameManager = gameManager;
        this.registry = registry;
        this.runtime = runtime;
        this.plugin = plugin;
        this.renderer = renderer;
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
        long now = tick.incrementAndGet();

        for (Player p : Bukkit.getOnlinePlayers()) {
            var s = sessions.get(p);
            if (s == null || s.state() != PlayerState.IN_GAME) continue;
            if (!allowsGameplaySkills(p, s)) continue;

            runtime.handle(new SkillContext(
                    p,
                    plugin,
                    new TickEvent(now),
                    null,
                    null,
                    now,
                    runtime.skillConfig()
            ));

            renderer.render(p, registry.skillsOf(p), now);
        }
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
