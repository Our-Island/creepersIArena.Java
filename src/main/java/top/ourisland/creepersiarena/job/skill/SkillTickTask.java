package top.ourisland.creepersiarena.job.skill;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;
import top.ourisland.creepersiarena.job.skill.event.SkillContext;
import top.ourisland.creepersiarena.job.skill.event.impl.TickEvent;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRuntime;
import top.ourisland.creepersiarena.job.skill.ui.SkillHotbarRenderer;

import java.util.concurrent.atomic.AtomicLong;

public final class SkillTickTask extends BukkitRunnable {

    private final Plugin plugin;
    private final PlayerSessionStore sessions;
    private final SkillRegistry registry;
    private final SkillRuntime runtime;
    private final SkillHotbarRenderer renderer;

    private final AtomicLong tick = new AtomicLong(0);

    public SkillTickTask(Plugin plugin,
                         PlayerSessionStore sessions,
                         SkillRegistry registry,
                         SkillRuntime runtime,
                         SkillHotbarRenderer renderer) {
        this.plugin = plugin;
        this.sessions = sessions;
        this.registry = registry;
        this.runtime = runtime;
        this.renderer = renderer;
    }

    public long nowTick() {
        return tick.get();
    }

    @Override
    public void run() {
        long now = tick.incrementAndGet();

        for (Player p : Bukkit.getOnlinePlayers()) {
            var s = sessions.get(p);
            if (s == null || s.state() != PlayerState.IN_GAME) continue;

            runtime.handle(new SkillContext(
                    p,
                    new TickEvent(now),
                    null,
                    null,
                    now
            ));

            renderer.render(p, registry.skillsOf(p), now);
        }
    }
}
