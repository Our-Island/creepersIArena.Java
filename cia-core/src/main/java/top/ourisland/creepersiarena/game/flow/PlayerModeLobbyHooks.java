package top.ourisland.creepersiarena.game.flow;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModePlayerFlow;
import top.ourisland.creepersiarena.api.game.mode.context.ModeLobbyContext;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;

import java.util.function.Supplier;

/**
 * Centralized access to mode-owned lobby/player-flow decisions.
 * <p>
 * Stage transitions and lobby transitions both call through this helper so hub, respawn, and in-game UI paths cannot
 * silently drift apart.
 */
final class PlayerModeLobbyHooks {

    private final Logger log;
    private final Supplier<GameRuntime> runtime;
    private final Supplier<IModePlayerFlow> playerFlow;

    PlayerModeLobbyHooks(
            @lombok.NonNull Logger log,
            @lombok.NonNull Supplier<GameRuntime> runtime,
            @lombok.NonNull Supplier<IModePlayerFlow> playerFlow
    ) {
        this.log = log;
        this.runtime = runtime;
        this.playerFlow = playerFlow;
    }

    boolean allowJobSelection(Player player, PlayerSession session) {
        GameRuntime rt = runtime.get();
        IModePlayerFlow flow = playerFlow.get();
        if (rt == null || flow == null) return session != null && session.state().isLobbyState();
        try {
            return flow.allowJobSelection(new ModeLobbyContext(rt, player, session));
        } catch (Throwable t) {
            log.warn("[LobbyHooks] mode job-selection hook failed: player={} err={}",
                    playerName(player), t.getMessage(), t
            );
            return session != null && session.state().isLobbyState();
        }
    }

    private String playerName(Player player) {
        return player == null ? "null" : player.getName();
    }

    boolean acceptsLobbyUiInput(Player player, PlayerSession session) {
        if (session == null) return false;
        GameRuntime rt = runtime.get();
        IModePlayerFlow flow = playerFlow.get();
        if (rt == null || flow == null) return defaultAcceptsLobbyUiInput(player, session);
        try {
            return flow.acceptsLobbyUiInput(new ModeLobbyContext(rt, player, session));
        } catch (Throwable t) {
            log.warn("[LobbyHooks] mode lobby-input hook failed: player={} err={}",
                    playerName(player), t.getMessage(), t
            );
            return defaultAcceptsLobbyUiInput(player, session);
        }
    }

    private boolean defaultAcceptsLobbyUiInput(Player player, PlayerSession session) {
        return session.state()
                .isLobbyState() || showJobSelector(player, session) || selectableTeamCount(player, session) > 0;
    }

    boolean showJobSelector(Player player, PlayerSession session) {
        GameRuntime rt = runtime.get();
        IModePlayerFlow flow = playerFlow.get();
        if (rt == null || flow == null) return session != null && session.state().isLobbyState();
        try {
            return flow.showJobSelector(new ModeLobbyContext(rt, player, session));
        } catch (Throwable t) {
            log.warn("[LobbyHooks] mode job-selector hook failed: player={} err={}",
                    playerName(player), t.getMessage(), t
            );
            return session != null && session.state().isLobbyState();
        }
    }

    int selectableTeamCount(Player player, PlayerSession session) {
        GameRuntime rt = runtime.get();
        IModePlayerFlow flow = playerFlow.get();
        if (rt == null || flow == null) return 0;
        try {
            return Math.max(0, flow.selectableTeamCount(new ModeLobbyContext(rt, player, session)));
        } catch (Throwable t) {
            log.warn("[LobbyHooks] mode selectable-team hook failed: player={} err={}",
                    playerName(player), t.getMessage(), t
            );
            return 0;
        }
    }

    void decorateLobbyInventory(Player player, PlayerSession session, PlayerInventory inventory) {
        if (inventory == null) return;
        GameRuntime rt = runtime.get();
        IModePlayerFlow flow = playerFlow.get();
        if (rt == null || flow == null) return;
        try {
            flow.decorateLobbyInventory(new ModeLobbyContext(rt, player, session), inventory);
        } catch (Throwable t) {
            log.warn("[LobbyHooks] mode lobby-decoration hook failed: player={} err={}",
                    playerName(player), t.getMessage(), t
            );
        }
    }

}
