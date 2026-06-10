package top.ourisland.creepersiarena.defaultcontent.cosmetic.particle;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.cosmetic.*;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;

import java.util.List;

public final class ConfiguredParticleCosmetic implements IParticleCosmetic {

    private final CosmeticId id;
    private final Component displayName;
    private final ItemStack icon;
    private final ParticleSchedule schedule;
    private final List<ParticleEmission> emissions;
    private final IAbilityGate abilities;

    public ConfiguredParticleCosmetic(
            CosmeticId id,
            Component displayName,
            ItemStack icon,
            ParticleSchedule schedule,
            List<ParticleEmission> emissions,
            IAbilityGate abilities
    ) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.schedule = schedule;
        this.emissions = emissions == null ? List.of() : List.copyOf(emissions);
        this.abilities = abilities;
    }

    @Override
    public CosmeticId id() {
        return id;
    }

    @Override
    public CosmeticSlot slot() {
        return CosmeticSlot.PARTICLE_TRAIL;
    }

    @Override
    public Component displayName() {
        return displayName;
    }

    @Override
    public ItemStack icon() {
        return icon.clone();
    }

    @Override
    public ParticleSchedule schedule() {
        return schedule;
    }

    @Override
    public void spawn(ParticleCosmeticContext context) {
        if (context == null) return;
        boolean enabled = context.player() == null
                ? abilities != null && abilities.isEnabledForGame(DefaultContentAbilities.PARTICLE_COSMETICS, "particle_cosmetic")
                : abilities != null && abilities.isEnabled(DefaultContentAbilities.PARTICLE_COSMETICS, context.player(), "particle_cosmetic");
        if (!enabled) return;
        emissions.forEach(emission -> emission.spawn(context));
    }

}
