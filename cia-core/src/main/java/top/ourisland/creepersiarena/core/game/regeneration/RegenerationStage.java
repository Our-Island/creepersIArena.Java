package top.ourisland.creepersiarena.core.game.regeneration;

import org.jspecify.annotations.NonNull;

public record RegenerationStage(
        int tick,
        int durationTicks,
        int amplifier,
        @NonNull String sound,
        float volume,
        float pitch
) {

}
