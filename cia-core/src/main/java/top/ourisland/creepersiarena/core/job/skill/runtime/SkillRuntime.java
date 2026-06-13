package top.ourisland.creepersiarena.core.job.skill.runtime;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.config.ISkillConfigView;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.SkillContext;
import top.ourisland.creepersiarena.api.skill.runtime.ISkillStateStore;
import top.ourisland.creepersiarena.api.skill.runtime.SkillActivationRejectedException;
import top.ourisland.creepersiarena.core.config.model.SkillConfig;

import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public final class SkillRuntime {

    private final SkillRegistry registry;
    private final ISkillStateStore store;
    private final DoubleSupplier cooldownFactor;
    private final BooleanSupplier cooldownEnabled;
    private final Supplier<? extends ISkillConfigView> skillConfig;
    private final Supplier<IAbilityGate> abilities;

    public SkillRuntime(
            @lombok.NonNull SkillRegistry registry,
            @lombok.NonNull ISkillStateStore store,
            @lombok.NonNull DoubleSupplier cooldownFactor,
            @lombok.NonNull BooleanSupplier cooldownEnabled,
            @lombok.NonNull Supplier<? extends ISkillConfigView> skillConfig,
            @lombok.NonNull Supplier<IAbilityGate> abilities
    ) {
        this.registry = registry;
        this.store = store;
        this.cooldownFactor = cooldownFactor;
        this.cooldownEnabled = cooldownEnabled;
        this.skillConfig = skillConfig;
        this.abilities = abilities;
    }

    public void handle(SkillContext ctx) {
        if (!skillRuntimeEnabled(ctx)) return;

        var p = ctx.player();
        var skills = registry.skillsOf(p);
        if (skills.isEmpty()) return;

        var pid = p.getUniqueId();
        long now = ctx.nowTick();

        double f = cooldownFactor.getAsDouble();
        if (Double.isNaN(f) || Double.isInfinite(f) || f < 0) f = 1.0;

        for (var def : skills) {
            if (def == null) continue;

            if (def.type() == SkillType.ACTIVE) {
                var src = ctx.sourceSkillId();
                if (src == null || !src.equals(def.id())) {
                    continue;
                }
            }

            if (!matchesAnyTrigger(def, ctx)) continue;

            int baseCdSec = cooldownEnabled.getAsBoolean()
                    ? Math.max(0, skillConfig().cooldownSeconds(def.id(), def.cooldownSeconds()))
                    : 0;
            int scaledCdSec = (baseCdSec == 0) ? 0 : (int) Math.ceil(baseCdSec * f);

            if (scaledCdSec > 0 && store.isCoolingDown(pid, def.id(), now)) {
                continue;
            }

            try {
                def.executor().execute(ctx, store);
            } catch (SkillActivationRejectedException _) {
                continue;
            }

            if (scaledCdSec > 0) {
                long endTick = now + (long) scaledCdSec * 20L;
                store.cooldownEndsAtTick(pid, def.id(), endTick);
            }
        }
    }

    private boolean skillRuntimeEnabled(SkillContext ctx) {
        return isRuntimeEnabled(ctx.player(), "skill_runtime");
    }

    private boolean matchesAnyTrigger(ISkillDefinition def, SkillContext ctx) {
        var triggers = def.triggers();
        if (triggers == null || triggers.isEmpty()) return false;
        for (var t : triggers) {
            try {
                if (t != null && t.matches(ctx)) return true;
            } catch (Throwable _) {
            }
        }
        return false;
    }

    public ISkillConfigView skillConfig() {
        try {
            var c = skillConfig.get();
            return c == null ? SkillConfig.defaults() : c;
        } catch (Throwable _) {
            return SkillConfig.defaults();
        }
    }

    public boolean isRuntimeEnabled(
            Player player,
            String reason
    ) {
        var gate = abilities.get();
        if (gate == null) return false;
        return gate.isEnabled(CoreAbilities.SKILL_RUNTIME, player, reason == null ? "skill_runtime" : reason);
    }

    public ISkillStateStore store() {
        return store;
    }

}
