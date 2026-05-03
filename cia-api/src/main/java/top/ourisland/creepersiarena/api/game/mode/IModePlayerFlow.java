package top.ourisland.creepersiarena.api.game.mode;

import org.bukkit.Location;
import org.bukkit.inventory.PlayerInventory;
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
     * Returns whether the hub entrance region may trigger this mode's join flow.
     * <p>
     * Entrance semantics are mode-owned because some modes, such as always-open battle arenas, treat the entrance as a
     * direct join, while round-based modes may expose only a ready/waiting UI in the hub.
     *
     * @param ctx lobby context
     *
     * @return {@code true} when the generic lobby entrance listener may request a join for this mode
     */
    default boolean allowHubEntrance(ModeLobbyContext ctx) {
        return false;
    }

    /**
     * Returns whether the generic lobby listener should protect and route lobby inventory input for this player.
     * <p>
     * Modes with custom lobby items can override this without exposing their item semantics to core. The default keeps
     * the previous core behaviour for lobby states and enabled core selectors.
     *
     * @param ctx lobby/player context
     *
     * @return {@code true} when core should cancel and route lobby inventory interactions
     */
    default boolean acceptsLobbyUiInput(ModeLobbyContext ctx) {
        return ctx != null
                && ctx.session() != null
                && (ctx.session().state().isLobbyState() || showJobSelector(ctx) || selectableTeamCount(ctx) > 0);
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
     * Lets a mode decorate the already-created generic lobby inventory with mode-owned controls.
     * <p>
     * Core still owns clearing/protecting the inventory and rendering generic selectors. Mode-specific ready buttons,
     * phase controls, or read-only indicators belong in the active mode and can be installed here.
     *
     * @param ctx       lobby/player context
     * @param inventory player inventory after generic core items have been applied
     */
    default void decorateLobbyInventory(ModeLobbyContext ctx, PlayerInventory inventory) {
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
     * Returns whether the shared job/skill runtime may render and tick gameplay skill items for this player right now.
     * <p>
     * Some modes move players into the generic {@code IN_GAME} stage for pre-round UI such as job selection. Those
     * phases should not receive loadout skill items, otherwise mode-owned selector items can be overwritten by the
     * global skill hotbar renderer. The default preserves historical behaviour for always-live combat modes.
     *
     * @param ctx player-flow context
     *
     * @return {@code true} when core skill ticking and hotbar rendering are allowed
     */
    default boolean allowGameplaySkillRuntime(ModePlayerContext ctx) {
        return true;
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
