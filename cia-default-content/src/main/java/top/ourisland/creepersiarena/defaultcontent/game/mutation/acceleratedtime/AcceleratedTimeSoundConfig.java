package top.ourisland.creepersiarena.defaultcontent.game.mutation.acceleratedtime;

import org.bukkit.configuration.ConfigurationSection;
import top.ourisland.creepersiarena.api.config.StrictConfig;

public record AcceleratedTimeSoundConfig(
        String sound,
        float volume,
        float pitch
) {

    public AcceleratedTimeSoundConfig {
        if (sound == null || sound.isBlank()) {
            throw new IllegalArgumentException("Accelerated-time sound id must not be blank");
        }
        if (!Float.isFinite(volume) || volume < 0.0F) {
            throw new IllegalArgumentException("Accelerated-time sound volume must be finite and >= 0: " + volume);
        }
        if (!Float.isFinite(pitch) || pitch <= 0.0F) {
            throw new IllegalArgumentException("Accelerated-time sound pitch must be finite and > 0: " + pitch);
        }
    }

    public static AcceleratedTimeSoundConfig startDefault() {
        return new AcceleratedTimeSoundConfig("minecraft:block.beacon.activate", 1.0F, 0.8F);
    }

    public static AcceleratedTimeSoundConfig endDefault() {
        return new AcceleratedTimeSoundConfig("minecraft:block.beacon.deactivate", 1.0F, 0.8F);
    }

    public static AcceleratedTimeSoundConfig fromSection(
            ConfigurationSection section,
            AcceleratedTimeSoundConfig defaults,
            String path
    ) {
        if (section == null) return defaults;
        return new AcceleratedTimeSoundConfig(
                StrictConfig.string(section, "sound", defaults.sound(), path + ".sound"),
                (float) StrictConfig.decimal(section, "volume", defaults.volume(), path + ".volume"),
                (float) StrictConfig.decimal(section, "pitch", defaults.pitch(), path + ".pitch")
        );
    }

}
