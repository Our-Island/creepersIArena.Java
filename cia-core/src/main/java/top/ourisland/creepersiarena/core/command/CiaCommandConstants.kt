package top.ourisland.creepersiarena.core.command;

import java.util.List;

/**
 * Shared command literals and small static suggestion sets.
 */
public final class CiaCommandConstants {

    public static final String
            PLAYER_ROOT_LITERAL = "cia",
            ADMIN_ROOT_LITERAL = "ciaa";

    /**
     * The command overhaul intentionally exposes only /cia and /ciaa roots. Standalone legacy aliases stay empty by
     * design.
     */
    public static final List<String>
            PLAYER_ROOT_ALIASES = List.of(),
            ADMIN_ROOT_ALIASES = List.of(),
            PLAYER_LANGUAGE_SUGGESTIONS = List.of("default", "en_us", "zh_cn"),
            ADMIN_LANGUAGE_SUGGESTIONS = List.of("en_us", "zh_cn"),
            CONFIG_TARGET_SUGGESTIONS = List.of("config", "arena", "skill"),
            BOOLEAN_SUGGESTIONS = List.of("true", "false"),
            PREFERENCE_BOOLEAN_SUGGESTIONS = List.of("on", "off", "true", "false"),
            TEAM_SUGGESTIONS = List.of(
                    "random",
                    "1", "2", "3", "4",
                    "red", "blue", "green", "yellow",
                    "aqua", "cyan", "purple", "white", "black"
            );

    private CiaCommandConstants() {
    }

}
