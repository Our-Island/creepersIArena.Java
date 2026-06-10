package top.ourisland.creepersiarena.game.ability;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.IAbilityConfigView;

public record BukkitAbilityConfigView(
        AbilityId id,
        @Nullable ConfigurationSection section
) implements IAbilityConfigView {

    @Override
    public boolean exists() {
        return section != null;
    }

    @Override
    public @Nullable ConfigurationSection settingsSection() {
        return section == null ? null : section.getConfigurationSection("settings");
    }

}
