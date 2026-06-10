package top.ourisland.creepersiarena.api.game.mutation;

import java.util.Locale;

public enum MutationClockMode {

    AUTO,
    VANILLA_TICK_RATE,
    LOGICAL;

    public static MutationClockMode fromConfig(String raw) {
        if (raw == null || raw.isBlank()) return AUTO;
        try {
            return MutationClockMode.valueOf(raw.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException _) {
            return AUTO;
        }
    }

}
