package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.context.ModeLobbyContext;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerState;

/**
 * Steal-owned lobby presentation.
 * <p>
 * The ready button itself is rendered and updated by {@link StealReadyController}; this class only decides when that
 * waiting UI belongs in a player's inventory.
 */
final class StealLobbyUi {

    private final StealState state;
    private final StealReadyController readyController;

    StealLobbyUi(StealState state, StealReadyController readyController) {
        this.state = state;
        this.readyController = readyController;
    }

    void refreshWaiting(Player player, PlayerSession session) {
        readyController.refreshWaiting(player, session);
    }

    void refreshWaiting(
            Player player,
            PlayerSession session,
            int ready,
            int required
    ) {
        readyController.refreshWaiting(player, session, ready, required);
    }

    int countReadyOnline() {
        return readyController.countReadyOnline();
    }

    int requiredReadyPlayers() {
        return readyController.requiredReadyPlayers();
    }

    boolean isReadyButton(ItemStack item) {
        return readyController.isReadyButton(item);
    }

    boolean toggleReady(Player player, GameSession session) {
        return readyController.toggleReady(player, session);
    }

    void decorate(ModeLobbyContext ctx, PlayerInventory inventory) {
        if (ctx == null || inventory == null) return;
        PlayerSession session = ctx.session();
        if (session == null || session.state() != PlayerState.HUB) return;
        if (!isWaitingPhase()) return;

        readyController.decorateWaitingInventory(session, inventory);
    }

    private boolean isWaitingPhase() {
        return state.phase == StealPhase.LOBBY || state.phase == StealPhase.START_COUNTDOWN;
    }

    boolean acceptsInput(PlayerSession session) {
        if (session == null) return false;
        if (session.state() == PlayerState.HUB && isWaitingPhase()) return true;
        return session.state() == PlayerState.IN_GAME
                && state.phase == StealPhase.CHOOSE_JOB
                && StealPlayerState.participant(session);
    }

}
