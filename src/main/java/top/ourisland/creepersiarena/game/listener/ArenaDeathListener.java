package top.ourisland.creepersiarena.game.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;

public final class ArenaDeathListener implements Listener {

    private final PlayerSessionStore store;
    private final GameFlow flow;

    public ArenaDeathListener(PlayerSessionStore store, GameFlow flow) {
        this.store = store;
        this.flow = flow;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        var p = e.getEntity();
        var session = store.get(p);
        if (session == null) return;
        if (session.state() != PlayerState.IN_GAME) return;

        e.getDrops().clear();
        e.setDroppedExp(0);

        flow.onPlayerDeath(p);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        var p = e.getPlayer();
        var s = store.get(p);
        if (s == null) return;

        flow.onPlayerRespawnEvent(p, e::setRespawnLocation);
    }
}
