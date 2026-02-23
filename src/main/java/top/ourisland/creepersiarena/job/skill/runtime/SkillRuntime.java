package top.ourisland.creepersiarena.job.skill.runtime;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.config.model.SkillConfig;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.ITrigger;
import top.ourisland.creepersiarena.job.skill.event.SkillContext;

import java.util.List;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public final class SkillRuntime {

    private final SkillRegistry registry;
    private final SkillStateStore store;
    private final DoubleSupplier cooldownFactor;
    private final Supplier<SkillConfig> skillConfig;

    public SkillRuntime(
            @lombok.NonNull SkillRegistry registry,
            @lombok.NonNull SkillStateStore store,
            @lombok.NonNull DoubleSupplier cooldownFactor,
            @lombok.NonNull Supplier<SkillConfig> skillConfig
    ) {
        this.registry = registry;
        this.store = store;
        this.cooldownFactor = cooldownFactor;
        this.skillConfig = skillConfig;
    }

    public void handle(SkillContext ctx) {
        Player p = ctx.player();
        List<ISkillDefinition> skills = registry.skillsOf(p);
        if (skills.isEmpty()) return;

        UUID pid = p.getUniqueId();
        long now = ctx.nowTick();

        double f = cooldownFactor.getAsDouble();
        if (Double.isNaN(f) || Double.isInfinite(f) || f < 0) f = 1.0;

        for (ISkillDefinition def : skills) {
            if (def == null) continue;

            if (def.type() == SkillType.ACTIVE) {
                String src = ctx.sourceSkillId();
                if (src == null || !src.equals(def.id())) {
                    continue;
                }
            }

            if (!matchesAnyTrigger(def, ctx)) continue;

            int baseCdSec = Math.max(0, skillConfig().cooldownSeconds(def.id(), def.cooldownSeconds()));
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

    private boolean matchesAnyTrigger(ISkillDefinition def, SkillContext ctx) {
        List<ITrigger> triggers = def.triggers();
        if (triggers == null || triggers.isEmpty()) return false;
        for (ITrigger t : triggers) {
            try {
                if (t != null && t.matches(ctx)) return true;
            } catch (Throwable _) {
            }
        }
        return false;
    }


    public SkillConfig skillConfig() {
        try {
            SkillConfig c = skillConfig.get();
            return c == null ? SkillConfig.defaults() : c;
        } catch (Throwable _) {
            return SkillConfig.defaults();
        }
    }

    public SkillStateStore store() {
        return store;
    }

}
