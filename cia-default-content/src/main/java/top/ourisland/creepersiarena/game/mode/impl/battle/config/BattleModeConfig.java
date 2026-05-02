package top.ourisland.creepersiarena.game.mode.impl.battle.config;

import top.ourisland.creepersiarena.api.config.GameConfigView;

/**
 * Battle-owned global mode configuration. This type intentionally lives with default content, not core.
 */
public record BattleModeConfig(
        int singleGameTimeSeconds,
        int respawnTimeSeconds,
        int maxTeam,
        boolean teamAutoBalancing,
        boolean forceBalancing
) {

    public static BattleModeConfig from(GameConfigView config) {
        return new BattleModeConfig(
                config.modeInt("battle", "single-game-time", 600),
                config.modeInt("battle", "respawn-time", 10),
                Math.max(1, config.modeInt("battle", "max-team", 4)),
                config.modeBoolean("battle", "team-auto-balancing", true),
                config.modeBoolean("battle", "force-balancing", false)
        );
    }

}
