package top.ourisland.creepersiarena.job.skill.runtime;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.job.skill.SkillDefinition;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.SkillContext;
import top.ourisland.creepersiarena.job.skill.event.Trigger;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class SkillRuntime {

    private final SkillRegistry registry;
    private final SkillStateStore store;

    public SkillRuntime(SkillRegistry registry, SkillStateStore store) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.store = Objects.requireNonNull(store, "store");
    }

    public void handle(SkillContext ctx) {
        Player p = ctx.player();
        List<SkillDefinition> skills = registry.skillsOf(p);
        if (skills.isEmpty()) return;

        UUID pid = p.getUniqueId();
        long now = ctx.nowTick();

        for (SkillDefinition def : skills) {
            if (def == null) continue;

            if (def.kind() == SkillType.ACTIVE) {
                String src = ctx.sourceSkillId();
                if (src == null || !src.equals(def.id())) {
                    continue;
                }
            }

            if (!matchesAnyTrigger(def, ctx)) continue;

            if (def.cooldownSeconds() > 0 && store.isCoolingDown(pid, def.id(), now)) {
                continue;
            }

            def.executor().execute(ctx, store);

            int cdSec = Math.max(0, def.cooldownSeconds());
            if (cdSec > 0) {
                long endTick = now + (long) cdSec * 20L;
                store.cooldownEndsAtTick(pid, def.id(), endTick);
            }
        }
    }

    private boolean matchesAnyTrigger(SkillDefinition def, SkillContext ctx) {
        List<Trigger> triggers = def.triggers();
        if (triggers == null || triggers.isEmpty()) return false;
        for (Trigger t : triggers) {
            try {
                if (t != null && t.matches(ctx)) return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    public SkillStateStore store() {
        return store;
    }
}
