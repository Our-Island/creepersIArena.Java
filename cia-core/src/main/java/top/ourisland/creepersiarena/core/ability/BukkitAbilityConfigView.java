package top.ourisland.creepersiarena.core.ability;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.IAbilityConfigView;
import top.ourisland.creepersiarena.api.config.StrictConfig;

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
        return StrictConfig.section(
                section,
                "settings",
                section == null ? "ability." + id.asString() + ".settings" : section.getCurrentPath() + ".settings"
        );
    }

}
