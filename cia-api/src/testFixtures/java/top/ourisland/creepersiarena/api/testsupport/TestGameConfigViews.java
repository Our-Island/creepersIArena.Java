package top.ourisland.creepersiarena.api.testsupport;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import top.ourisland.creepersiarena.api.config.IGameConfigView;

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
            public boolean isModeDisabled(String modeId) {
                return yaml.getBoolean("game.modes." + normalizeModeId(modeId) + ".disabled", false);
            }

            @Override
            public int leaveDelaySeconds() {
                return yaml.getInt("game.leave-delay-seconds", 0);
            }

            @Override
            public ConfigurationSection modeSection(String modeId) {
                return yaml.getConfigurationSection("game.modes." + normalizeModeId(modeId));
            }
        };
    }

    public static String normalizeModeId(String modeId) {
        if (modeId == null || modeId.isBlank()) return "";
        int separator = modeId.indexOf(':');
        return separator >= 0 ? modeId.substring(separator + 1) : modeId;
    }

}
