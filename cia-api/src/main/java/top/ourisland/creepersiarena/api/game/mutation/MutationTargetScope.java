package top.ourisland.creepersiarena.api.game.mutation;

import java.util.Locale;

public enum MutationTargetScope {

    ACTIVE_GAME_PLAYERS,
    ALL_ONLINE;

    public static MutationTargetScope fromConfig(String raw) {
        if (raw == null || raw.isBlank()) return ACTIVE_GAME_PLAYERS;
        try {
            return MutationTargetScope.valueOf(raw.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException _) {
            return ACTIVE_GAME_PLAYERS;
        }
    }

}
