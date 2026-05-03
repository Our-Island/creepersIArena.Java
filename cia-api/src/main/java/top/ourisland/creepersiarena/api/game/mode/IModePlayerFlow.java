package top.ourisland.creepersiarena.api.game.mode;

import org.bukkit.Location;
import top.ourisland.creepersiarena.api.game.mode.context.ModeLobbyContext;
import top.ourisland.creepersiarena.api.game.mode.context.ModePlayerContext;

/**
 * Mode-owned player transition hooks.
 * <p>
 * The core flow should not know how a particular mode picks spawn points, equips a player, shows mode-specific UI, or
 * stores mode-local state. It should only move the player into the generic {@code IN_GAME} stage and delegate those
 * gameplay-specific details to this contract.
 */
public interface IModePlayerFlow {

    IModePlayerFlow DEFAULT = new IModePlayerFlow() {
    };

    /**
     * Selects the location used when a player enters or returns to the active game.
     *
     * @param ctx player-flow context
     *
     * @return spawn location; returning {@code null} falls back to {@link ModePlayerContext#fallback()}
     */
    default Location spawnLocation(ModePlayerContext ctx) {
        if (ctx == null || ctx.game() == null || ctx.game().arena() == null) {
            return ctx == null ? null : ctx.fallback();
        }

        var session = ctx.session();
        if (session != null) {
            String key = session.selectedTeamKey();
            if (key == null && session.selectedTeam() != null) {
                key = String.valueOf(session.selectedTeam());
            }
            if (key != null) {
                var group = ctx.game().arena().spawnGroup(key);
                if (!group.isEmpty()) {
                    return group.getFirst().clone();
                }
            }
        }

        return ctx.game().arena().firstSpawnOrAnchor("default");
    }

    /**
     * Returns how many teams this mode allows players to select from in the generic lobby UI.
     * <p>
     * Returning {@code 0} hides the core-provided team selector. Modes with custom lobby/team selection flows can keep
     * this disabled and expose their own controls through their extension.
     *
     * @param ctx lobby context
     *
     * @return selectable team count; {@code 0} means no generic team selector
     */
    default int selectableTeamCount(ModeLobbyContext ctx) {
        return 0;
    }

    /**
     * Returns whether job-selector items and job-selection commands are accepted right now.
     * <p>
     * Implementations usually return the same value as {@link #showJobSelector(ModeLobbyContext)}. The method is
     * separate so a mode can render a read-only selector or accept a command while rendering the UI elsewhere.
     *
     * @param ctx lobby/player context
     *
     * @return {@code true} when a job selection may update the player's selected job
     */
    default boolean allowJobSelection(ModeLobbyContext ctx) {
        return showJobSelector(ctx);
    }

    /**
     * Returns whether the core-provided job selector should be shown for the supplied player state.
     * <p>
     * The default preserves the historical behaviour: jobs can be picked in normal lobby states. Modes with their own
     * round flow can move this UI into a mode phase, for example a pre-round job-selection phase, and keep HUB/RESPAWN
     * inventories clean.
     *
     * @param ctx lobby/player context
     *
     * @return {@code true} when core should render job-selection items for this player
     */
    default boolean showJobSelector(ModeLobbyContext ctx) {
        return ctx != null && ctx.session() != null && ctx.session().state().isLobbyState();
    }

    /**
     * Applies mode-specific player state after core has moved the player into the generic in-game stage.
     * <p>
     * Typical implementations equip a loadout, update mode-local session data, show mode-specific UI, or set the Bukkit
     * game mode. The default hook intentionally does nothing.
     *
     * @param ctx player-flow context
     */
    default void onEnterGame(ModePlayerContext ctx) {
    }

}
