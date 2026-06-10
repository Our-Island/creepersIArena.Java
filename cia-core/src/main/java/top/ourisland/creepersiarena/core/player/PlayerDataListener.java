package top.ourisland.creepersiarena.core.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerDataListener implements Listener {

    private final PlayerDataService playerData;

    public PlayerDataListener(PlayerDataService playerData) {
        this.playerData = playerData;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        playerData.loadAsync(player.getUniqueId(), player.getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerData.saveAndUnloadAsync(event.getPlayer().getUniqueId());
    }

}
