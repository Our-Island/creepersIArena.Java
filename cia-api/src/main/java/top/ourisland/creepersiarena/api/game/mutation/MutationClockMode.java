package top.ourisland.creepersiarena.api.game.mutation;

import java.util.Locale;

public enum MutationClockMode {

    AUTO,
    VANILLA_TICK_RATE,
    LOGICAL;

    public static MutationClockMode fromConfig(String raw) {
        if (raw == null) return AUTO;
        if (raw.isBlank()) {
            throw new IllegalArgumentException("MutationClockMode must not be blank");
        }
        try {
            return MutationClockMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid MutationClockMode value: " + raw, exception);
        }
    }

}
