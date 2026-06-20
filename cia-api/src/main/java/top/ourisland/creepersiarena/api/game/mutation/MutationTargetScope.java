package top.ourisland.creepersiarena.api.game.mutation;

import java.util.Locale;

public enum MutationTargetScope {

    ACTIVE_GAME_PLAYERS,
    ALL_ONLINE;

    public static MutationTargetScope fromConfig(String raw) {
        if (raw == null) return ACTIVE_GAME_PLAYERS;
        if (raw.isBlank()) {
            throw new IllegalArgumentException("MutationTargetScope must not be blank");
        }
        try {
            return MutationTargetScope.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid MutationTargetScope value: " + raw, exception);
        }
    }

}
