package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemService;
import top.ourisland.creepersiarena.utils.Msg;

/**
 * Owns steal's waiting ready control: state transitions, item detection, and inventory rendering.
 */
final class StealReadyController {

    private final GameRuntime runtime;
    private final StealState state;
    private final LobbyItemService lobbyItems;
    private final ConfigManager configManager;
    private final StealReadyItemCodec readyItems = new StealReadyItemCodec();

    StealReadyController(GameRuntime runtime, StealState state) {
        this.runtime = runtime;
        this.state = state;
        this.lobbyItems = runtime.requireService(LobbyItemService.class);
        this.configManager = runtime.requireService(ConfigManager.class);
    }

    boolean isReadyButton(ItemStack item) {
        return readyItems.isReadyButton(item);
    }

    void refreshWaiting(Player player, PlayerSession session) {
        refreshWaiting(player, session, countReadyOnline(), requiredReadyPlayers());
    }

    void refreshWaiting(
            Player player,
            PlayerSession session,
            int ready,
            int required
    ) {
        if (player == null || session == null || session.state() != PlayerState.HUB) return;
        if (!isWaitingPhase()) return;

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

    private boolean isWaitingPhase() {
        return state.phase == StealPhase.LOBBY || state.phase == StealPhase.START_COUNTDOWN;
    }

    void decorateWaitingInventory(
            PlayerSession session,
            PlayerInventory inventory,
            int ready,
            int required
    ) {
        if (session == null || inventory == null || !isWaitingPhase()) return;
        inventory.setItem(0, readyItems.readyButton(StealPlayerState.ready(session), ready, required));
    }

    void decorateWaitingInventory(PlayerSession session, PlayerInventory inventory) {
        decorateWaitingInventory(session, inventory, countReadyOnline(), requiredReadyPlayers());
    }

    boolean toggleReady(Player player, GameSession game) {
        if (player == null || game == null) return false;
        PlayerSession session = runtime.sessionStore().getOrCreate(player);
        if (!canToggleReady(session)) {
            Msg.actionBar(player, Component.text("当前阶段不能切换准备", NamedTextColor.RED));
            return false;
        }

        boolean next = !StealPlayerState.ready(session);
        StealPlayerState.ready(session, next);
        StealPlayerState.participant(session, false);
        StealPlayerState.alive(session, false);
        if (!game.players().contains(player.getUniqueId())) {
            game.addPlayer(player.getUniqueId());
        }

        decorateWaitingInventory(session, player.getInventory(), countReadyOnline(), requiredReadyPlayers());
        player.playSound(player, next
                ? Sound.BLOCK_NOTE_BLOCK_PLING
                : Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.PLAYERS, 1.0f, next ? 2.0f : 1.0f);
        Msg.actionBar(player, next
                ? Component.text("✪ 你已准备加入偷窃模式", NamedTextColor.GREEN)
                : Component.text("✪ 你取消了准备", NamedTextColor.GRAY));
        return true;
    }

    private boolean canToggleReady(PlayerSession session) {
        return session != null && session.state() == PlayerState.HUB && isWaitingPhase();
    }

}
