package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.context.ModeLobbyContext;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemService;

/**
 * Steal-owned lobby presentation.
 * <p>
 * Core still provides the low-level inventory clearing and the generic two-team cycle button, while the ready control
 * is owned and marked by steal so it cannot be confused with enter-game actions.
 */
final class StealLobbyUi {

    private final GameRuntime runtime;
    private final StealState state;
    private final LobbyItemService lobbyItems;
    private final ConfigManager configManager;
    private final StealReadyItemCodec readyItems = new StealReadyItemCodec();

    StealLobbyUi(GameRuntime runtime, StealState state) {
        this.runtime = runtime;
        this.state = state;
        this.lobbyItems = runtime.requireService(LobbyItemService.class);
        this.configManager = runtime.requireService(ConfigManager.class);
    }

    void refreshWaiting(Player player, PlayerSession session) {
        refreshWaiting(player, session, countReadyOnline(), requiredReadyPlayers());
    }

    void refreshWaiting(Player player, PlayerSession session, int ready, int required) {
        if (player == null || session == null || session.state() != PlayerState.HUB) return;
        if (state.phase != StealPhase.LOBBY && state.phase != StealPhase.START_COUNTDOWN) return;

        lobbyItems.applyHubKit(player, session, configManager.globalConfig(), 2, false);
        decorateWaitingInventory(session, player.getInventory(), ready, required);
    }

    int countReadyOnline() {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerSession session = runtime.sessionStore().get(player);
            if (StealPlayerState.ready(session)) count++;
        }
        return count;
    }

    int requiredReadyPlayers() {
        return state.modeConfig().requiredReadyPlayers(Bukkit.getOnlinePlayers().size());
    }

    private void decorateWaitingInventory(PlayerSession session, PlayerInventory inventory, int ready, int required) {
        inventory.setItem(0, readyItems.readyButton(StealPlayerState.ready(session), ready, required));
    }

    void decorate(ModeLobbyContext ctx, PlayerInventory inventory) {
        if (ctx == null || inventory == null) return;
        PlayerSession session = ctx.session();
        if (session == null || session.state() != PlayerState.HUB) return;
        if (state.phase != StealPhase.LOBBY && state.phase != StealPhase.START_COUNTDOWN) return;

        decorateWaitingInventory(session, inventory, countReadyOnline(), requiredReadyPlayers());
    }

    boolean acceptsInput(PlayerSession session) {
        if (session == null) return false;
        if (session.state() == PlayerState.HUB
                && (state.phase == StealPhase.LOBBY || state.phase == StealPhase.START_COUNTDOWN)) {
            return true;
        }
        return session.state() == PlayerState.IN_GAME
                && state.phase == StealPhase.CHOOSE_JOB
                && StealPlayerState.participant(session);
    }

}
