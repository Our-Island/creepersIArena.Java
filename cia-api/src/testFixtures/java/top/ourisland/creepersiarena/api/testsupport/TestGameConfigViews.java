package top.ourisland.creepersiarena.api.testsupport;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import top.ourisland.creepersiarena.api.config.IGameConfigView;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.identity.CiaConfigPaths;

/**
 * Shared config-view fixtures for mode/config tests.
 */
public final class TestGameConfigViews {

    private TestGameConfigViews() {
    }

    public static IGameConfigView empty() {
        return fromYaml(new YamlConfiguration());
    }

    public static IGameConfigView fromYaml(YamlConfiguration yaml) {
        return new IGameConfigView() {
            @Override
            public boolean isModeDisabled(GameModeId modeId) {
                return yaml.getBoolean("game.modes.%s.disabled".formatted(CiaConfigPaths.section(modeId)), false);
            }

            @Override
            public int leaveDelaySeconds() {
                return yaml.getInt("game.leave-delay-seconds", 0);
            }

            @Override
            public ConfigurationSection modeSection(GameModeId modeId) {
                return yaml.getConfigurationSection("game.modes." + CiaConfigPaths.section(modeId));
            }
        };
    }

}
