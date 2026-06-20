package top.ourisland.creepersiarena.core.game.flow;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModePlayerFlow;
import top.ourisland.creepersiarena.api.game.mode.context.ModeLobbyContext;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.team.TeamId;

import java.util.LinkedHashSet;
import java.util.List;

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
        var rt = runtime.get();
        var flow = playerFlow.get();
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
        var rt = runtime.get();
        var flow = playerFlow.get();
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
        return session.state().isLobbyState()
                || showJobSelector(player, session)
                || !selectableTeams(player, session).isEmpty();
    }

    boolean showJobSelector(Player player, PlayerSession session) {
        var rt = runtime.get();
        var flow = playerFlow.get();
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

    List<TeamId> selectableTeams(Player player, PlayerSession session) {
        var rt = runtime.get();
        var flow = playerFlow.get();
        if (rt == null || flow == null) return List.of();
        try {
            var supplied = flow.selectableTeams(new ModeLobbyContext(rt, player, session));
            if (supplied == null || supplied.isEmpty()) return List.of();
            var unique = new LinkedHashSet<TeamId>();
            for (var team : supplied) {
                if (team == null) {
                    throw new IllegalStateException("Mode returned a null selectable team");
                }
                if (!unique.add(team)) {
                    throw new IllegalStateException("Mode returned duplicate selectable team: " + team);
                }
            }
            return List.copyOf(unique);
        } catch (Throwable t) {
            log.warn("[LobbyHooks] mode selectable-team hook failed: player={} err={}",
                    playerName(player), t.getMessage(), t
            );
            return List.of();
        }
    }

    void decorateLobbyInventory(
            Player player,
            PlayerSession session,
            PlayerInventory inventory
    ) {
        if (inventory == null) return;
        var rt = runtime.get();
        var flow = playerFlow.get();
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
