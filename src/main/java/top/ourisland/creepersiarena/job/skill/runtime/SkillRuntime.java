package top.ourisland.creepersiarena.job.skill.runtime;

import lombok.NonNull;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.job.skill.SkillDefinition;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.SkillContext;
import top.ourisland.creepersiarena.job.skill.event.Trigger;

import java.util.List;
import java.util.UUID;
import java.util.function.DoubleSupplier;

public final class SkillRuntime {

    private final SkillRegistry registry;
    private final SkillStateStore store;
    private final DoubleSupplier cooldownFactor;

    public SkillRuntime(
            @NonNull SkillRegistry registry,
            @NonNull SkillStateStore store,
            @NonNull DoubleSupplier cooldownFactor
    ) {
        this.registry = registry;
        this.store = store;
        this.cooldownFactor = cooldownFactor;
    }

    public void handle(SkillContext ctx) {
        Player p = ctx.player();
        List<SkillDefinition> skills = registry.skillsOf(p);
        if (skills.isEmpty()) return;

        UUID pid = p.getUniqueId();
        long now = ctx.nowTick();

        double f = cooldownFactor.getAsDouble();
        if (Double.isNaN(f) || Double.isInfinite(f) || f < 0) f = 1.0;

        for (SkillDefinition def : skills) {
            if (def == null) continue;

            if (def.kind() == SkillType.ACTIVE) {
                String src = ctx.sourceSkillId();
                if (src == null || !src.equals(def.id())) {
                    continue;
                }
            }

            if (!matchesAnyTrigger(def, ctx)) continue;

            int baseCdSec = Math.max(0, def.cooldownSeconds());
            int scaledCdSec = (baseCdSec == 0) ? 0 : (int) Math.ceil(baseCdSec * f);

            if (scaledCdSec > 0 && store.isCoolingDown(pid, def.id(), now)) {
                continue;
            }

            def.executor().execute(ctx, store);

            if (scaledCdSec > 0) {
                long endTick = now + (long) scaledCdSec * 20L;
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
