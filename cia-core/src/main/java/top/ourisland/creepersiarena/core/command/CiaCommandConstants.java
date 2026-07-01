package top.ourisland.creepersiarena.core.command;

import java.util.List;

/**
 * Shared command literals, aliases and small static suggestion sets.
 *
 * <p>The first command refactor stage keeps the runtime behaviour intact while
 * moving previously inline constants out of the command tree implementation.</p>
 */
public final class CiaCommandConstants {

    public static final String PLAYER_ROOT_LITERAL = "creepersia";
    public static final String ADMIN_ROOT_LITERAL = "ciaa";
    public static final String ADMIN_EMBEDDED_LITERAL = "admin";

    public static final List<String> PLAYER_ROOT_ALIASES = List.of("cia", "creepersiarena");
    public static final List<String> NO_ALIASES = List.of();
    public static final List<String> TEAM_SUGGESTIONS = List.of(
            "random",
            "1", "2", "3", "4",
            "red", "blue", "green", "yellow",
            "aqua", "cyan", "purple", "white", "black"
    );
    public static final List<String> PLAYER_LANGUAGE_SUGGESTIONS = List.of("default", "en_us", "zh_cn");
    public static final List<String> ADMIN_LANGUAGE_SUGGESTIONS = List.of("en_us", "zh_cn");
    public static final List<String> CONFIG_TARGET_SUGGESTIONS = List.of("config", "arena", "skill");
    public static final List<String> BOOLEAN_SUGGESTIONS = List.of("true", "false");

    private CiaCommandConstants() {
    }

}
