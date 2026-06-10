package top.ourisland.creepersiarena.job.skill.runtime;

import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.config.ISkillConfigView;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.SkillContext;
import top.ourisland.creepersiarena.api.skill.runtime.ISkillStateStore;
import top.ourisland.creepersiarena.api.skill.runtime.SkillActivationRejectedException;
import top.ourisland.creepersiarena.config.model.SkillConfig;

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
        List<ISkillDefinition> skills = registry.skillsOf(p);
        if (skills.isEmpty()) return;

        UUID pid = p.getUniqueId();
        long now = ctx.nowTick();

        double f = cooldownFactor.getAsDouble();
        if (Double.isNaN(f) || Double.isInfinite(f) || f < 0) f = 1.0;

        for (var def : skills) {
            if (def == null) continue;

            if (def.type() == SkillType.ACTIVE) {
                String src = ctx.sourceSkillId();
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
        var gate = abilities.get();
        if (gate == null) return true;
        return gate.isEnabled(CoreAbilities.SKILL_RUNTIME, ctx.player(), "skill_runtime");
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


    public ISkillConfigView skillConfig() {
        try {
            ISkillConfigView c = skillConfig.get();
            return c == null ? SkillConfig.defaults() : c;
        } catch (Throwable _) {
            return SkillConfig.defaults();
        }
    }

    public ISkillStateStore store() {
        return store;
    }

}
