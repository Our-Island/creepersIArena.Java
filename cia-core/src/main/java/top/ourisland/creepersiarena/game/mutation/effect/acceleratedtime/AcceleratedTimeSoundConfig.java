package top.ourisland.creepersiarena.game.mutation.effect.acceleratedtime;

import org.bukkit.configuration.ConfigurationSection;

public record AcceleratedTimeSoundConfig(
        String sound,
        float volume,
        float pitch
) {

    public AcceleratedTimeSoundConfig {
        if (sound == null || sound.isBlank()) sound = "minecraft:block.beacon.activate";
        volume = Math.max(0.0F, volume);
    }

    public static AcceleratedTimeSoundConfig startDefault() {
        return new AcceleratedTimeSoundConfig("minecraft:block.beacon.activate", 1.0F, 0.8F);
    }

    public static AcceleratedTimeSoundConfig endDefault() {
        return new AcceleratedTimeSoundConfig("minecraft:block.beacon.deactivate", 1.0F, 0.8F);
    }

    public static AcceleratedTimeSoundConfig fromSection(
            ConfigurationSection section,
            AcceleratedTimeSoundConfig fallback
    ) {
        if (section == null) return fallback;
        return new AcceleratedTimeSoundConfig(
                section.getString("sound", fallback.sound()),
                (float) section.getDouble("volume", fallback.volume()),
                (float) section.getDouble("pitch", fallback.pitch())
        );
    }

}
